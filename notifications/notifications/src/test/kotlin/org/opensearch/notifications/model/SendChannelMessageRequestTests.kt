/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.createObjectFromJsonString
import org.opensearch.notifications.index.NotificationConfigIndex
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SendChannelMessageRequestTests {
    private val request = SendChannelMessageRequest(
        "config_id",
        EventSource("title", "reference_id", SeverityType.HIGH, listOf("tag")),
        ChannelMessage("text", "<p>text</p>", null)
    )

    private fun assertRequestEquals(expected: SendChannelMessageRequest, actual: SendChannelMessageRequest) {
        assertEquals(expected.configId, actual.configId)
        assertEquals(expected.eventSource, actual.eventSource)
        assertEquals(expected.channelMessage, actual.channelMessage)
    }

    @Test
    fun `request serialize and deserialize transport object should be equal`() {
        val recreated = recreateObject(request) { SendChannelMessageRequest(it) }
        assertRequestEquals(request, recreated)
    }

    @Test
    fun `request parses the body with the channel taken from the path`() {
        val json = """
            {
                "event_source":{"title":"title","reference_id":"reference_id","severity":"high","tags":["tag"]},
                "channel_message":{"text_description":"text","html_description":"<p>text</p>"}
            }
        """.trimIndent()
        val parsed = createObjectFromJsonString(json) { SendChannelMessageRequest.parse(it, "config_id") }
        assertRequestEquals(request, parsed)
    }

    @Test
    fun `request parse rejects any field other than the event source and the message`() {
        listOf(""""context":"admin||all_access"""", """"channel_id_list":["other"]""", """"config_id":"other"""").forEach {
            val json = """
                {
                    "event_source":{"title":"t","reference_id":"r","severity":"info","tags":[]},
                    "channel_message":{"text_description":"text"},
                    $it
                }
            """.trimIndent()
            assertThrows<IllegalArgumentException> {
                createObjectFromJsonString(json) { SendChannelMessageRequest.parse(it, "config_id") }
            }
        }
    }

    @Test
    fun `request names one channel config as its document`() {
        assertEquals(NotificationConfigIndex.INDEX_NAME, request.index())
        assertEquals("config_id", request.id())
        assertEquals("notification_config", request.type())
    }

    @Test
    fun `request validate requires a config id`() {
        assertNull(request.validate())
        val blank = SendChannelMessageRequest(" ", request.eventSource, request.channelMessage)
        assertNotNull(blank.validate())
    }
}
