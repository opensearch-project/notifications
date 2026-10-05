/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
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
import org.opensearch.commons.notifications.action.NotificationsActions
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
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.security.UserAccessManager
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

internal class SendNotificationActionTests {
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
    private lateinit var action: SendNotificationAction

    @BeforeEach
    fun setup() {
        threadContext = ThreadContext(Settings.EMPTY)
        val threadPool = mock(ThreadPool::class.java)
        `when`(threadPool.threadContext).thenReturn(threadContext)
        val client = mock(Client::class.java)
        `when`(client.threadPool()).thenReturn(threadPool)
        action = SendNotificationAction(
            mock(TransportService::class.java),
            client,
            ActionFilters(setOf()),
            mock(NamedXContentRegistry::class.java)
        )
        mockkObject(SendMessageActionHelper)
        mockkObject(UserAccessManager)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    private fun execute(request: SendNotificationRequest): SendNotificationResponse {
        val future = PlainActionFuture<SendNotificationResponse>()
        action.execute(mock(Task::class.java), request, future)
        return future.get(10, TimeUnit.SECONDS)
    }

    @Test
    fun `send in the caller's security context is authorized as the caller for the channel`() {
        val sent = slot<SendNotificationRequest>()
        coEvery { UserAccessManager.verifyResourceAccess(any(), any()) } just runs
        coEvery { SendMessageActionHelper.executeRequest(capture(sent)) } returns response
        threadContext.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, callerUserInfo)

        val actual = execute(SendNotificationRequest(eventSource, channelMessage, listOf("a"), null))

        assertEquals(response, actual)
        assertEquals(callerUserInfo, sent.captured.threadContext)
        coVerify(exactly = 1) { UserAccessManager.verifyResourceAccess("a", NotificationsActions.SEND_NOTIFICATION_NAME) }
    }

    @Test
    fun `send without a caller security context keeps the user the request acts for`() {
        val sent = slot<SendNotificationRequest>()
        coEvery { SendMessageActionHelper.executeRequest(capture(sent)) } returns response
        val inProcessUser = "bob|marketing|alerting_full_access"

        execute(SendNotificationRequest(eventSource, channelMessage, listOf("a"), inProcessUser))

        assertEquals(inProcessUser, sent.captured.threadContext)
        coVerify(exactly = 0) { UserAccessManager.verifyResourceAccess(any(), any()) }
    }

    @Test
    fun `send without any user context is not checked per channel`() {
        val sent = slot<SendNotificationRequest>()
        coEvery { SendMessageActionHelper.executeRequest(capture(sent)) } returns response

        execute(SendNotificationRequest(eventSource, channelMessage, listOf("a"), null))

        assertNull(sent.captured.threadContext)
        coVerify(exactly = 0) { UserAccessManager.verifyResourceAccess(any(), any()) }
    }

    @Test
    fun `send fails without delivering when the caller cannot access the channel`() {
        coEvery { UserAccessManager.verifyResourceAccess("a", any()) } throws
            OpenSearchStatusException("no permissions", RestStatus.FORBIDDEN)
        threadContext.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, callerUserInfo)

        val error = assertThrows<ExecutionException> {
            execute(SendNotificationRequest(eventSource, channelMessage, listOf("a"), null))
        }

        assertEquals(RestStatus.FORBIDDEN, (error.cause as OpenSearchStatusException).status())
        coVerify(exactly = 0) { SendMessageActionHelper.executeRequest(any()) }
    }
}
