/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.send

import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.SeverityType

/**
 * Helper function for send transport action.
 */
object SendTestNotificationActionHelper {
    fun generateMessage(configId: String): ChannelMessage {
        return ChannelMessage(
            getMessageTextDescription(configId),
            getMessageHtmlDescription(configId),
            null
        )
    }

    fun generateEventSource(configId: String): EventSource {
        return EventSource(
            getMessageTitle(configId),
            configId,
            SeverityType.INFO
        )
    }

    private fun getMessageTitle(configId: String): String {
        return "Test Message Title-$configId" // TODO: change as spec
    }

    private fun getMessageTextDescription(configId: String): String {
        return "Test message content body for config id $configId" // TODO: change as spec
    }

    private fun getMessageHtmlDescription(configId: String): String {
        return """
            <html>
            <header><title>Test Message</title></header>
            <body>
            <p>Test Message for config id $configId</p>
            </body>
            </html>
        """.trimIndent() // TODO: change as spec
    }
}
