/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.send

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.CHIME_URL
import org.opensearch.notifications.CoreProvider
import org.opensearch.notifications.SLACK_URL
import org.opensearch.notifications.TEAMS_URL
import org.opensearch.notifications.WEBHOOK_URL
import org.opensearch.notifications.createSlackNotificationConfig
import org.opensearch.notifications.createWebhookNotificationConfig
import org.opensearch.notifications.index.ConfigOperations
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.security.UserAccess
import org.opensearch.notifications.spi.NotificationCore
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.BaseDestination
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.MicrosoftTeamsDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.util.ConfigEncryptionTransformer
import org.opensearch.notifications.util.FieldEncryptionService
import java.time.Instant
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Suppress("TooManyFunctions")
internal class SendMessageDecryptionTests {

    private fun generateKey(): SecretKey = KeyGenerator.getInstance("AES").also { it.init(256) }.generateKey()

    private val okResponse = DestinationMessageResponse(RestStatus.OK.status, "OK")

    private val permissiveUserAccess = object : UserAccess {
        override fun validateUser(user: org.opensearch.commons.authuser.User?) = Unit
        override fun getAllAccessInfo(user: org.opensearch.commons.authuser.User?) = emptyList<String>()
        override fun getSearchAccessInfo(user: org.opensearch.commons.authuser.User?) = emptyList<String>()
        override fun doesUserHaveAccess(user: org.opensearch.commons.authuser.User?, access: List<String>) = true
    }

    private val mockConfigOps: ConfigOperations = mockk()
    private val mockCore: NotificationCore = mockk()

    private val capturedDestination = slot<BaseDestination>()

    @BeforeEach
    fun setUp() {
        mockkObject(CoreProvider)
        every { CoreProvider.core } returns mockCore
        every {
            mockCore.sendMessage(capture(capturedDestination), any<MessageContent>(), any<String>())
        } returns okResponse

        SendMessageActionHelper.initialize(mockConfigOps, permissiveUserAccess, ConfigEncryptionTransformer)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(CoreProvider)
    }

    private fun buildRequest(): SendNotificationRequest {
        val channelId = "some-id"
        val eventSource = EventSource("Test Title", "ref-$channelId", SeverityType.INFO)
        val channelMessage = ChannelMessage("Test message body", null, null)
        return SendNotificationRequest(eventSource, channelMessage, listOf(channelId), "")
    }

    private fun docInfoFor(id: String) = DocInfo(id = id)
    private fun metadataFor() = DocMetadata(Instant.now(), Instant.now(), emptyList())
    private fun channelDocInfo(id: String = "doesnt-matter", config: NotificationConfig) = NotificationConfigDocInfo(docInfoFor(id), NotificationConfigDoc(metadataFor(), config))

    private fun mockReadConfigById(answer: NotificationConfig) {
        coEvery { mockConfigOps.getNotificationConfig(any()) } returns channelDocInfo(config = answer)
    }

    @Nested
    inner class SlackDecryption {

        @Test
        fun `encrypted Slack URL is decrypted before the message is sent`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            val encryptedUrl = fes.encrypt(SLACK_URL)
            val config = createSlackNotificationConfig(encryptedUrl)
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val actualDestination = capturedDestination.captured as SlackDestination
            assertEquals(SLACK_URL, actualDestination.url)
        }

        @Test
        fun `plaintext Slack URL (passthrough mode) is forwarded unchanged`() {
            ConfigEncryptionTransformer.initialize(FieldEncryptionService(null))
            val config = createSlackNotificationConfig(SLACK_URL)
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val actualDestination = capturedDestination.captured as SlackDestination
            assertEquals(SLACK_URL, actualDestination.url)
        }

        @Test
        fun `Slack URL encrypted with previousKey is decrypted during key rotation`() {
            val oldKey = generateKey()
            val newKey = generateKey()
            val oldFes = FieldEncryptionService(oldKey)
            val rotatingFes = FieldEncryptionService(newKey, oldKey)
            ConfigEncryptionTransformer.initialize(rotatingFes)
            val encryptedWithOldKey = oldFes.encrypt(SLACK_URL)
            val config = createSlackNotificationConfig(encryptedWithOldKey)
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val actualDestination = capturedDestination.captured as SlackDestination
            assertEquals(SLACK_URL, actualDestination.url)
        }
    }

    @Nested
    inner class ChimeDecryption {

        @Test
        fun `encrypted Chime URL is decrypted before the message is sent`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            val encryptedUrl = fes.encrypt(CHIME_URL)
            val config = NotificationConfig(
                "chime-channel",
                "desc",
                ConfigType.CHIME,
                configData = Chime(encryptedUrl)
            )
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val destination = capturedDestination.captured as ChimeDestination
            assertEquals(CHIME_URL, destination.url)
        }
    }

    @Nested
    inner class MicrosoftTeamsDecryption {

        @Test
        fun `encrypted Microsoft Teams URL is decrypted before the message is sent`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            val encryptedUrl = fes.encrypt(TEAMS_URL)
            val config = NotificationConfig(
                "msteams-channel",
                "desc",
                ConfigType.MICROSOFT_TEAMS,
                configData = MicrosoftTeams(encryptedUrl)
            )
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val destination = capturedDestination.captured as MicrosoftTeamsDestination
            assertEquals(TEAMS_URL, destination.url)
        }
    }

    @Nested
    inner class WebhookDecryption {

        @Test
        fun `encrypted Webhook URL and header values are all decrypted before the message is sent`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            val expectedAuthHeader = "Bearer super-secret-token"
            val expectedApiKey = "my-api-key-12345"
            val encryptedUrl = fes.encrypt(WEBHOOK_URL)
            val encryptedAuthHeader = fes.encrypt(expectedAuthHeader)
            val encryptedApiKey = fes.encrypt(expectedApiKey)
            val headers = mapOf(
                "Authorization" to encryptedAuthHeader,
                "X-Api-Key" to encryptedApiKey
            )
            val config = createWebhookNotificationConfig(encryptedUrl, headers)
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val destination = capturedDestination.captured as CustomWebhookDestination
            assertEquals(WEBHOOK_URL, destination.url)
            assertEquals(expectedAuthHeader, destination.headerParams["Authorization"])
            assertEquals(expectedApiKey, destination.headerParams["X-Api-Key"])
        }

        @Test
        fun `Webhook with no header params - only URL is decrypted`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            val encryptedUrl = fes.encrypt(WEBHOOK_URL)
            val config = createWebhookNotificationConfig(encryptedUrl)
            mockReadConfigById(config)

            runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }

            val destination = capturedDestination.captured as CustomWebhookDestination
            assertEquals(WEBHOOK_URL, destination.url)
        }
    }

    @Nested
    inner class DecryptionErrorHandling {

        @Test
        fun `tampered ciphertext causes an exception before the message is sent`() {
            val key = generateKey()
            val fes = FieldEncryptionService(key)
            ConfigEncryptionTransformer.initialize(fes)
            // Produce valid ciphertext then corrupt it
            val validEncryptedURL = fes.encrypt(SLACK_URL)
            val prefix = FieldEncryptionService.ENCRYPTED_PREFIX
            val tamperedCipherText = prefix + validEncryptedURL.removePrefix(prefix).reversed()
            val config = createSlackNotificationConfig(tamperedCipherText)
            mockReadConfigById(config)

            assertThrows<Exception> {
                runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }
            }
        }

        @Test
        fun `value encrypted with wrong key causes an exception before the message is sent`() {
            val correctKey = generateKey()
            val wrongKey = generateKey()
            // Config was encrypted with wrongKey but service is initialized with correctKey
            val encryptedWithWrongKey = FieldEncryptionService(wrongKey).encrypt(
                "https://hooks.slack.com/services/TOKEN"
            )
            ConfigEncryptionTransformer.initialize(FieldEncryptionService(correctKey))
            val config = createSlackNotificationConfig(encryptedWithWrongKey)
            mockReadConfigById(config)

            assertThrows<Exception> {
                runBlocking { SendMessageActionHelper.executeRequest(buildRequest()) }
            }
        }
    }
}
