/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.support.ActionFilters
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.notifications.model.ReencryptNotificationConfigsRequest
import org.opensearch.notifications.util.ConfigEncryptionTransformer
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client
import java.time.Instant

@ExtendWith(MockitoExtension::class)
internal class ReencryptNotificationConfigsActionTests {

    @Mock private lateinit var transportService: TransportService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: Client
    private val actionFilters = ActionFilters(setOf())

    private lateinit var action: ReencryptNotificationConfigsAction

    @BeforeEach
    fun setUp() {
        mockkObject(NotificationConfigIndex)
        mockkObject(ConfigEncryptionTransformer)
        action = ReencryptNotificationConfigsAction(transportService, client, actionFilters)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(NotificationConfigIndex)
        unmockkObject(ConfigEncryptionTransformer)
    }

    private fun slackConfigInfo(id: String, url: String): NotificationConfigInfo {
        val config = NotificationConfig(
            "channel-$id",
            "description",
            ConfigType.SLACK,
            configData = Slack(url)
        )
        return NotificationConfigInfo(id, Instant.now(), Instant.now(), config)
    }

    private fun searchResult(docs: List<NotificationConfigInfo>, total: Long = docs.size.toLong()) =
        NotificationConfigSearchResult(0L, total, TotalHits.Relation.EQUAL_TO, docs)

    private fun executeRequest() = runBlocking {
        action.executeRequest(ReencryptNotificationConfigsRequest(), user = null)
    }

    @Nested
    inner class EmptyIndex {

        @Test
        fun `returns all-zero counts when index has no documents`() {
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(emptyList(), 0L)

            val response = executeRequest()

            assertEquals(0, response.migrated)
            assertEquals(0, response.skipped)
            assertEquals(0, response.failed)
            assertEquals(0, response.remaining)
        }

        @Test
        fun `does not call updateNotificationConfig when index is empty`() {
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(emptyList(), 0L)

            executeRequest()

            coVerify(exactly = 0) { NotificationConfigIndex.updateNotificationConfig(any(), any()) }
        }
    }

    @Nested
    inner class AllSkipped {

        @Test
        fun `counts all docs as skipped when no re-encryption is needed`() {
            val docs = listOf(slackConfigInfo("id-1", "https://hooks.slack.com/1"), slackConfigInfo("id-2", "https://hooks.slack.com/2"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns false

            val response = executeRequest()

            assertEquals(0, response.migrated)
            assertEquals(2, response.skipped)
            assertEquals(0, response.failed)
            assertEquals(0, response.remaining)
        }

        @Test
        fun `does not call updateNotificationConfig when all docs are already on active key`() {
            val docs = listOf(slackConfigInfo("id-1", "https://hooks.slack.com/1"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns false

            executeRequest()

            coVerify(exactly = 0) { NotificationConfigIndex.updateNotificationConfig(any(), any()) }
        }
    }

    @Nested
    inner class AllMigrated {

        @Test
        fun `counts all docs as migrated when every config needs re-encryption and update succeeds`() {
            val docs = listOf(slackConfigInfo("id-1", "enc:v1:old1"), slackConfigInfo("id-2", "enc:v1:old2"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(any()) } answers { firstArg() }
            coEvery { NotificationConfigIndex.updateNotificationConfig(any(), any()) } returns true

            val response = executeRequest()

            assertEquals(2, response.migrated)
            assertEquals(0, response.skipped)
            assertEquals(0, response.failed)
            assertEquals(0, response.remaining)
        }

        @Test
        fun `calls updateNotificationConfig once per config that needs re-encryption`() {
            val docs = listOf(slackConfigInfo("id-1", "enc:v1:old1"), slackConfigInfo("id-2", "enc:v1:old2"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(any()) } answers { firstArg() }
            coEvery { NotificationConfigIndex.updateNotificationConfig(any(), any()) } returns true

            executeRequest()

            coVerify(exactly = 2) { NotificationConfigIndex.updateNotificationConfig(any(), any()) }
        }
    }

    @Nested
    inner class UpdateFailure {

        @Test
        fun `increments failed and remaining when updateNotificationConfig returns false`() {
            val docs = listOf(slackConfigInfo("id-1", "enc:v1:old1"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(any()) } answers { firstArg() }
            coEvery { NotificationConfigIndex.updateNotificationConfig(any(), any()) } returns false

            val response = executeRequest()

            assertEquals(0, response.migrated)
            assertEquals(0, response.skipped)
            assertEquals(1, response.failed)
            assertEquals(1, response.remaining)
        }
    }

    @Nested
    inner class ExceptionHandling {

        @Test
        fun `increments failed when decryptConfig throws`() {
            val docs = listOf(slackConfigInfo("id-1", "enc:v1:corrupted"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(any()) } throws javax.crypto.AEADBadTagException("bad tag")

            val response = executeRequest()

            assertEquals(0, response.migrated)
            assertEquals(0, response.skipped)
            assertEquals(1, response.failed)
            assertEquals(1, response.remaining)
        }

        @Test
        fun `continues processing remaining docs after one failure`() {
            val docs = listOf(slackConfigInfo("id-fail", "enc:v1:bad"), slackConfigInfo("id-ok", "enc:v1:good"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(match { it.configData == Slack("enc:v1:bad") }) } throws
                RuntimeException("decrypt failed")
            every { ConfigEncryptionTransformer.decryptConfig(match { it.configData == Slack("enc:v1:good") }) } answers { firstArg() }
            coEvery { NotificationConfigIndex.updateNotificationConfig(any(), any()) } returns true

            val response = executeRequest()

            assertEquals(1, response.migrated)
            assertEquals(0, response.skipped)
            assertEquals(1, response.failed)
            assertEquals(1, response.remaining)
        }
    }

    @Nested
    inner class MixedResult {

        @Test
        fun `correctly tallies migrated, skipped, and failed across a mixed batch`() {
            // 3 docs: one needs re-encryption and succeeds, one is already current, one fails to update
            val docMigrate = slackConfigInfo("id-migrate", "enc:v1:old")
            val docSkip = slackConfigInfo("id-skip", "https://plain-url")
            val docFail = slackConfigInfo("id-fail", "enc:v1:bad")
            val docs = listOf(docMigrate, docSkip, docFail)

            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(docs)
            every { ConfigEncryptionTransformer.needsReencryption(docMigrate.notificationConfig) } returns true
            every { ConfigEncryptionTransformer.needsReencryption(docSkip.notificationConfig) } returns false
            every { ConfigEncryptionTransformer.needsReencryption(docFail.notificationConfig) } returns true
            every { ConfigEncryptionTransformer.decryptConfig(docMigrate.notificationConfig) } answers { firstArg() }
            every { ConfigEncryptionTransformer.decryptConfig(docFail.notificationConfig) } throws RuntimeException("oops")
            coEvery { NotificationConfigIndex.updateNotificationConfig("id-migrate", any()) } returns true

            val response = executeRequest()

            assertEquals(1, response.migrated)
            assertEquals(1, response.skipped)
            assertEquals(1, response.failed)
            assertEquals(1, response.remaining)
        }
    }

    @Nested
    inner class Pagination {

        @Test
        fun `fetches a second page when total exceeds first-page size`() {
            val page1 = (1..3).map { slackConfigInfo("p1-$it", "enc:v1:old-$it") }
            val page2 = (1..2).map { slackConfigInfo("p2-$it", "enc:v1:old-$it") }

            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(page1, 5L)
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 3 }) } returns searchResult(page2, 5L)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns false

            val response = executeRequest()

            assertEquals(0, response.migrated)
            assertEquals(5, response.skipped)
        }

        @Test
        fun `stops fetching when an empty page is returned`() {
            val page1 = listOf(slackConfigInfo("id-1", "https://url"))
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(page1, 100L)
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 1 }) } returns searchResult(emptyList(), 100L)
            every { ConfigEncryptionTransformer.needsReencryption(any()) } returns false

            val response = executeRequest()

            // Only the 1 doc from page1 was counted
            assertEquals(1, response.skipped)
            coVerify(exactly = 2) { NotificationConfigIndex.getAllNotificationConfigs(any(), any()) }
        }

        @Test
        fun `aggregates counts correctly across multiple pages`() {
            val page1 = listOf(slackConfigInfo("a", "enc:v1:old"))
            val page2 = listOf(slackConfigInfo("b", "https://plain"))

            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 0 }) } returns searchResult(page1, 2L)
            coEvery { NotificationConfigIndex.getAllNotificationConfigs(emptyList(), match { it.fromIndex == 1 }) } returns searchResult(page2, 2L)
            every { ConfigEncryptionTransformer.needsReencryption(page1[0].notificationConfig) } returns true
            every { ConfigEncryptionTransformer.needsReencryption(page2[0].notificationConfig) } returns false
            every { ConfigEncryptionTransformer.decryptConfig(any()) } answers { firstArg() }
            coEvery { NotificationConfigIndex.updateNotificationConfig(any(), any()) } returns true

            val response = executeRequest()

            assertEquals(1, response.migrated)
            assertEquals(1, response.skipped)
            assertEquals(0, response.failed)
        }
    }
}
