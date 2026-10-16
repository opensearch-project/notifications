/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.send

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipient
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.index.ConfigOperations
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.security.UserAccess
import org.opensearch.notifications.security.UserAccessManager
import java.time.Instant

internal class SendMessageActionHelperTests {
    private val action = "cluster:admin/opensearch/notifications/channel/send"
    private val request = SendNotificationRequest(
        EventSource("title", "reference_id", SeverityType.INFO),
        ChannelMessage("text", null, null),
        listOf("channel"),
        null
    )

    private lateinit var configOperations: ConfigOperations

    @BeforeEach
    fun setup() {
        configOperations = mockk()
        val userAccess = mockk<UserAccess>()
        every { userAccess.validateUser(any()) } just runs
        every { userAccess.doesUserHaveAccess(any(), any()) } returns true
        SendMessageActionHelper.initialize(configOperations, userAccess)
        mockkObject(UserAccessManager)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    /** A muted email channel: delivery stops at the mute check, after its child configs are resolved. */
    private fun mutedEmailChannel(): NotificationConfigDocInfo {
        val config = NotificationConfig(
            "email",
            "",
            ConfigType.EMAIL,
            Email("account", listOf(), listOf("group")),
            isEnabled = false
        )
        val metadata = DocMetadata(Instant.now(), Instant.now(), listOf())
        return NotificationConfigDocInfo(DocInfo("channel"), NotificationConfigDoc(metadata, config))
    }

    /** An enabled email channel with no direct recipients, so delivery only fans out to the given groups. */
    private fun emailChannel(groupIds: List<String>): NotificationConfigDocInfo {
        val config = NotificationConfig("email", "", ConfigType.EMAIL, Email("account", listOf(), groupIds))
        val metadata = DocMetadata(Instant.now(), Instant.now(), listOf())
        return NotificationConfigDocInfo(DocInfo("channel"), NotificationConfigDoc(metadata, config))
    }

    private fun childConfig(id: String, config: NotificationConfig): NotificationConfigDocInfo {
        val metadata = DocMetadata(Instant.now(), Instant.now(), listOf())
        return NotificationConfigDocInfo(DocInfo(id), NotificationConfigDoc(metadata, config))
    }

    private val account = childConfig(
        "account",
        NotificationConfig("account", "", ConfigType.SMTP_ACCOUNT, SmtpAccount("smtp.domain.com", 4321, MethodType.SSL, "from@domain.com"))
    )

    private fun group(id: String) = childConfig(
        id,
        NotificationConfig(id, "", ConfigType.EMAIL_GROUP, EmailGroup(listOf(EmailRecipient("$id@domain.com"))))
    )

    private fun sendExpectingStatus(childConfigAccessAction: String?): OpenSearchStatusException =
        assertThrows {
            runBlocking { SendMessageActionHelper.executeRequest(request, childConfigAccessAction) }
        }

    @Test
    fun `send checks the account and the groups of an email channel it delivers to`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("group", "deleted"))
        coEvery { configOperations.getNotificationConfigs(setOf("account", "group", "deleted")) } returns
            listOf(account, group("group"))
        coEvery { UserAccessManager.hasResourceAccess("account", action) } returns true
        coEvery { UserAccessManager.hasResourceAccess("group", action) } returns false

        sendExpectingStatus(action)

        coVerify(exactly = 1) { UserAccessManager.hasResourceAccess("account", action) }
        coVerify(exactly = 1) { UserAccessManager.hasResourceAccess("group", action) }
        coVerify(exactly = 0) { UserAccessManager.hasResourceAccess("deleted", any()) }
    }

    @Test
    fun `send to a muted email channel checks no child configs`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns mutedEmailChannel()
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(account, group("group"))

        val error = sendExpectingStatus(action)

        assertEquals(RestStatus.LOCKED, error.status())
        coVerify(exactly = 0) { UserAccessManager.hasResourceAccess(any(), any()) }
    }

    @Test
    fun `an account the caller cannot access is reported as access denied for the sender`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("group"))
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(account, group("group"))
        coEvery { UserAccessManager.hasResourceAccess("account", action) } returns false

        val error = sendExpectingStatus(action)

        assertEquals(RestStatus.FORBIDDEN, error.status())
        assertTrue(error.message!!.contains("Access denied for sender account"))
        // Still an email status, with the error on a placeholder recipient
        assertTrue(error.message!!.contains("\"config_type\":\"email\""))
        assertTrue(error.message!!.contains("invalid-access@example.com"))
        coVerify(exactly = 0) { UserAccessManager.hasResourceAccess("group", any()) }
    }

    @Test
    fun `a failed access check counts as denied for that config only`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("group"))
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(account, group("group"))
        coEvery { UserAccessManager.hasResourceAccess("account", action) } throws RuntimeException("security plugin unavailable")

        val error = sendExpectingStatus(action)

        assertEquals(RestStatus.FORBIDDEN, error.status())
        assertTrue(error.message!!.contains("Access denied for sender account"))
    }

    @Test
    fun `an account that no longer exists is reported as sender not found`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("group"))
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(group("group"))

        val error = sendExpectingStatus(null)

        assertEquals(RestStatus.NOT_FOUND, error.status())
        assertTrue(error.message!!.contains("Sender account not found"))
        assertTrue(error.message!!.contains("\"config_type\":\"email\""))
        assertTrue(error.message!!.contains("unknown-sender@example.com"))
    }

    @Test
    fun `each group is reported on its own instead of failing the whole send`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("denied", "deleted"))
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(account, group("denied"))
        coEvery { UserAccessManager.hasResourceAccess("account", action) } returns true
        coEvery { UserAccessManager.hasResourceAccess("denied", action) } returns false

        val error = sendExpectingStatus(action)

        assertEquals(RestStatus.MULTI_STATUS, error.status())
        assertTrue(error.message!!.contains("Access denied for recipient denied"))
        assertTrue(error.message!!.contains("Recipient deleted not found"))
    }

    @Test
    fun `send without a child config action does not check resource access`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns emailChannel(listOf("deleted"))
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf(account)

        val error = sendExpectingStatus(null)

        assertEquals(RestStatus.NOT_FOUND, error.status())
        coVerify(exactly = 0) { UserAccessManager.hasResourceAccess(any(), any()) }
    }

    @Test
    fun `send to a single muted channel returns locked`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns mutedEmailChannel()
        coEvery { configOperations.getNotificationConfigs(any()) } returns listOf()

        val error = sendExpectingStatus(null)

        assertEquals(RestStatus.LOCKED, error.status())
        assertTrue(error.message!!.contains("The channel is muted"))
    }

    @Test
    fun `send to a single missing channel returns not found`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns null

        val error = sendExpectingStatus(null)

        assertEquals(RestStatus.NOT_FOUND, error.status())
    }
}
