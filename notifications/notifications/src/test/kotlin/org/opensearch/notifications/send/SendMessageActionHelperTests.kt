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
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SeverityType
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

    private fun sendExpectingStatus(childConfigAccessAction: String?): OpenSearchStatusException =
        assertThrows {
            runBlocking { SendMessageActionHelper.executeRequest(request, childConfigAccessAction) }
        }

    @Test
    fun `send verifies the email channel's account and groups for the given action`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns mutedEmailChannel()
        coEvery { configOperations.getNotificationConfigs(setOf("account", "group")) } returns listOf()
        coEvery { UserAccessManager.verifyResourceAccess(any(), any()) } just runs

        sendExpectingStatus(action)

        coVerify(exactly = 1) { UserAccessManager.verifyResourceAccess("account", action) }
        coVerify(exactly = 1) { UserAccessManager.verifyResourceAccess("group", action) }
    }

    @Test
    fun `send stops before loading child configs the caller cannot access`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns mutedEmailChannel()
        coEvery { UserAccessManager.verifyResourceAccess("account", action) } throws
            OpenSearchStatusException("no permissions", RestStatus.FORBIDDEN)

        val error = sendExpectingStatus(action)

        assertEquals(RestStatus.FORBIDDEN, error.status())
        coVerify(exactly = 0) { configOperations.getNotificationConfigs(any()) }
    }

    @Test
    fun `send without a child config action does not verify child configs`() {
        coEvery { configOperations.getNotificationConfig("channel") } returns mutedEmailChannel()
        coEvery { configOperations.getNotificationConfigs(setOf("account", "group")) } returns listOf()

        sendExpectingStatus(null)

        coVerify(exactly = 0) { UserAccessManager.verifyResourceAccess(any(), any()) }
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
