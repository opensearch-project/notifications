/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.opensearch.OpenSearchStatusException
import org.opensearch.action.support.ActionFilters
import org.opensearch.action.support.PlainActionFuture
import org.opensearch.common.settings.Settings
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.model.SendChannelMessageRequest
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

internal class SendChannelMessageActionTests {
    private val callerUserInfo = "alice|engineering|notifications_send_access"
    private val eventSource = EventSource("title", "reference_id", severity = SeverityType.INFO)
    private val channelMessage = ChannelMessage("text", null, null)
    private val response = SendNotificationResponse(
        NotificationEvent(
            eventSource,
            listOf(EventStatus("a", "name", ConfigType.WEBHOOK, deliveryStatus = DeliveryStatus("200", "ok")))
        )
    )

    private lateinit var threadContext: ThreadContext
    private lateinit var action: SendChannelMessageAction

    @BeforeEach
    fun setup() {
        threadContext = ThreadContext(Settings.EMPTY)
        val threadPool = mock(ThreadPool::class.java)
        `when`(threadPool.threadContext).thenReturn(threadContext)
        val client = mock(Client::class.java)
        `when`(client.threadPool()).thenReturn(threadPool)
        action = SendChannelMessageAction(mock(TransportService::class.java), client, ActionFilters(setOf()))
        mockkObject(SendMessageActionHelper)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    private fun execute(request: SendChannelMessageRequest): SendNotificationResponse {
        val future = PlainActionFuture<SendNotificationResponse>()
        action.execute(mock(Task::class.java), request, future)
        return future.get(10, TimeUnit.SECONDS)
    }

    @Test
    fun `send delivers to the one channel as the authenticated caller`() {
        val sent = slot<SendNotificationRequest>()
        coEvery { SendMessageActionHelper.executeRequest(capture(sent), any()) } returns response
        threadContext.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, callerUserInfo)

        val actual = execute(SendChannelMessageRequest("a", eventSource, channelMessage))

        assertEquals(response, actual)
        assertEquals(listOf("a"), sent.captured.channelIds)
        assertEquals(eventSource, sent.captured.eventSource)
        assertEquals(channelMessage, sent.captured.channelMessage)
        assertEquals(callerUserInfo, sent.captured.threadContext)
    }

    @Test
    fun `send verifies the channel's child configs for the send action`() {
        coEvery { SendMessageActionHelper.executeRequest(any(), any()) } returns response

        execute(SendChannelMessageRequest("a", eventSource, channelMessage))

        coVerify(exactly = 1) {
            SendMessageActionHelper.executeRequest(any(), SendChannelMessageAction.NAME)
        }
    }

    @Test
    fun `send without the security plugin carries no user`() {
        val sent = slot<SendNotificationRequest>()
        coEvery { SendMessageActionHelper.executeRequest(capture(sent), any()) } returns response

        execute(SendChannelMessageRequest("a", eventSource, channelMessage))

        assertNull(sent.captured.threadContext)
    }

    @Test
    fun `a delivery failure is returned with its status`() {
        coEvery { SendMessageActionHelper.executeRequest(any(), any()) } throws
            OpenSearchStatusException("no permissions", RestStatus.FORBIDDEN)

        val error = assertThrows<ExecutionException> {
            execute(SendChannelMessageRequest("a", eventSource, channelMessage))
        }

        assertEquals(RestStatus.FORBIDDEN, (error.cause as OpenSearchStatusException).status())
    }
}
