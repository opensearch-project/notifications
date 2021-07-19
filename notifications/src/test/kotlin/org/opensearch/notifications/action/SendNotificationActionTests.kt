/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */
package org.opensearch.notifications.action

import com.nhaarman.mockitokotlin2.verify
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

@ExtendWith(MockitoExtension::class)
internal class SendNotificationActionTests {

    @Mock
    private lateinit var transportService: TransportService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: Client

    @Mock
    private lateinit var xContentRegistry: NamedXContentRegistry

    @Mock
    private lateinit var listener: ActionListener<SendNotificationResponse>

    private val actionFilters = ActionFilters(setOf())

    private lateinit var sendNotificationAction: SendNotificationAction

    @BeforeEach
    fun setUp() {
        sendNotificationAction = SendNotificationAction(
                transportService, client, actionFilters, xContentRegistry)
    }

    @Test
    fun doExecute() {
        val notificationId = "notification-1"
        val task = mock(Task::class.java)
        val request = createSendNotificationRequest()
        val response = SendNotificationResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(SendMessageActionHelper)
        every { SendMessageActionHelper.executeRequest(request) } returns response

        sendNotificationAction.execute(task, request, listener)
        verify(listener, times(1)).onResponse(eq(response))
    }

    private fun createSendNotificationRequest(): SendNotificationRequest {
        val notificationInfo = EventSource(
                "title",
                "reference_id",
                Feature.REPORTS,
                SeverityType.HIGH,
                listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
                "text_description",
                "<b>htmlDescription</b>",
                null
        )
        return SendNotificationRequest(
                notificationInfo,
                channelMessage,
                listOf("channelId1", "channelId2"),
                "sample-thread-context"
        )
    }
}
