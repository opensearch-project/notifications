/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.resthandler

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.core.common.bytes.BytesArray
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.test.rest.FakeRestChannel
import org.opensearch.test.rest.FakeRestRequest
import org.opensearch.transport.client.node.NodeClient

internal class SendNotificationRestHandlerTests {
    private val handler = SendNotificationRestHandler()

    private val validBody = """
        {
            "event_source":{"title":"title","reference_id":"id","severity":"info","tags":[]},
            "channel_message":{"text_description":"text"}
        }
    """.trimIndent()

    @Test
    fun `handler registers the send route with the channel in the path`() {
        assertEquals("notifications_send", handler.name)
        assertEquals(1, handler.routes().size)
        assertEquals(RestRequest.Method.POST, handler.routes()[0].method)
        assertEquals("$PLUGIN_BASE_URI/feature/send/{config_id}", handler.routes()[0].path)
    }

    @Test
    fun `handler sends to the channel named in the path`() {
        val client = mockk<NodeClient>(relaxed = true)
        val request = sendRequest(validBody, "a1b2c3")

        handler.handleRequest(request, FakeRestChannel(request, true, 1), client)

        val sent = slot<SendNotificationRequest>()
        verify { client.execute(NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE, capture(sent), any()) }
        assertEquals(listOf("a1b2c3"), sent.captured.channelIds)
        assertEquals("title", sent.captured.eventSource.title)
        assertEquals("text", sent.captured.channelMessage.textDescription)
        assertNull(sent.captured.threadContext)
    }

    @Test
    fun `handler rejects a user context in the request body`() {
        val body = """
            {
                "event_source":{"title":"title","reference_id":"id","severity":"info","tags":[]},
                "channel_message":{"text_description":"text"},
                "context":"admin||all_access"
            }
        """.trimIndent()
        val request = sendRequest(body, "a1b2c3")

        assertThrows<IllegalArgumentException> {
            handler.handleRequest(request, FakeRestChannel(request, true, 1), mockk<NodeClient>(relaxed = true))
        }
    }

    @Test
    fun `handler rejects a channel list in the request body`() {
        val body = """
            {
                "event_source":{"title":"title","reference_id":"id","severity":"info","tags":[]},
                "channel_message":{"text_description":"text"},
                "channel_id_list":["d4e5f6"]
            }
        """.trimIndent()
        val request = sendRequest(body, "a1b2c3")

        assertThrows<IllegalArgumentException> {
            handler.handleRequest(request, FakeRestChannel(request, true, 1), mockk<NodeClient>(relaxed = true))
        }
    }

    @Test
    fun `handler requires an event source and a channel message`() {
        val noEventSource = sendRequest("""{"channel_message":{"text_description":"text"}}""", "a1b2c3")
        assertThrows<IllegalArgumentException> {
            handler.handleRequest(noEventSource, FakeRestChannel(noEventSource, true, 1), mockk<NodeClient>(relaxed = true))
        }

        val noMessage = sendRequest(
            """{"event_source":{"title":"t","reference_id":"r","severity":"info","tags":[]}}""",
            "a1b2c3"
        )
        assertThrows<IllegalArgumentException> {
            handler.handleRequest(noMessage, FakeRestChannel(noMessage, true, 1), mockk<NodeClient>(relaxed = true))
        }
    }

    private fun sendRequest(body: String, configId: String): RestRequest =
        FakeRestRequest.Builder(NamedXContentRegistry.EMPTY)
            .withMethod(RestRequest.Method.POST)
            .withPath("$PLUGIN_BASE_URI/feature/send/$configId")
            .withParams(mapOf("config_id" to configId))
            .withContent(BytesArray(body), XContentType.JSON)
            .build()
}
