/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.resthandler

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.OpenSearchStatusException
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.common.bytes.BytesArray
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.action.SendChannelMessageAction
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.model.SendChannelMessageRequest
import org.opensearch.notifications.settings.PluginSettings
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

    @BeforeEach
    fun enableTheSendApi() {
        mockkObject(PluginSettings)
        every { PluginSettings.isSendApiEnabled() } returns true
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `handler refuses while the send api is disabled, before reading the body or counting the send`() {
        every { PluginSettings.isSendApiEnabled() } returns false
        val client = mockk<NodeClient>(relaxed = true)
        val request = sendRequest("{\"not\":\"a send request\"}", "a1b2c3")
        val sendsBefore = Metrics.NOTIFICATIONS_SEND_CHANNEL_MESSAGE_TOTAL.counter.getValue()

        val error = assertThrows<OpenSearchStatusException> {
            handler.handleRequest(request, FakeRestChannel(request, true, 1), client)
        }

        assertEquals(RestStatus.FORBIDDEN, error.status())
        assertEquals(sendsBefore, Metrics.NOTIFICATIONS_SEND_CHANNEL_MESSAGE_TOTAL.counter.getValue())
        verify(exactly = 0) { client.execute(SendChannelMessageAction.ACTION_TYPE, any(), any()) }
    }

    @Test
    fun `handler registers the send route with the channel in the path`() {
        assertEquals("notifications_send", handler.name)
        assertEquals(1, handler.routes().size)
        assertEquals(RestRequest.Method.POST, handler.routes()[0].method)
        assertEquals("$PLUGIN_BASE_URI/feature/send/{config_id}", handler.routes()[0].path)
    }

    @Test
    fun `handler sends to the channel named in the path through the channel send action`() {
        val client = mockk<NodeClient>(relaxed = true)
        val request = sendRequest(validBody, "a1b2c3")
        val sendsBefore = Metrics.NOTIFICATIONS_SEND_CHANNEL_MESSAGE_TOTAL.counter.getValue()

        handler.handleRequest(request, FakeRestChannel(request, true, 1), client)

        assertEquals(sendsBefore + 1, Metrics.NOTIFICATIONS_SEND_CHANNEL_MESSAGE_TOTAL.counter.getValue())

        val sent = slot<SendChannelMessageRequest>()
        verify { client.execute(SendChannelMessageAction.ACTION_TYPE, capture(sent), any()) }
        assertEquals("a1b2c3", sent.captured.configId)
        assertEquals("title", sent.captured.eventSource.title)
        assertEquals("text", sent.captured.channelMessage.textDescription)
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
