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

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class SendNotificationActionTests {

    @Mock
    private lateinit var transportService: TransportService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: Client

    @Mock
    private lateinit var xContentRegistry: NamedXContentRegistry

    private val actionFilters = ActionFilters(setOf())

    private lateinit var sendNotificationAction: SendNotificationAction

    @BeforeEach
    fun setUp() {
        sendNotificationAction = SendNotificationAction(
            transportService, client, actionFilters, xContentRegistry
        )
    }

    @Test
    fun doExecute() {
        val notificationId = "notification-1"
        val task = mock(Task::class.java)
        val request = mock(SendNotificationRequest::class.java)
        val response = SendNotificationResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(SendMessageActionHelper)
        every { SendMessageActionHelper.executeRequest(request) } returns response

        // Assert on response rather than verify it called which is better but harder
        // because the execute() runs in async CoroutineScope
        sendNotificationAction.execute(
            task, request,
            object : ActionListener<SendNotificationResponse> {
                override fun onResponse(actual: SendNotificationResponse?) {
                    assertEquals(response, actual)
                }
                override fun onFailure(error: Exception?) {
                    fail("Unexpected error happened", error)
                }
            }
        )
    }
}
