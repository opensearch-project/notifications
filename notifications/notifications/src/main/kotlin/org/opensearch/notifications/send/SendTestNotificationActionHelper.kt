package org.opensearch.notifications.send

import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.SeverityType

/**
 * Helper function for send transport action.
 */
object SendTestNotificationActionHelper {

    fun generateMessage(feature: Feature, configId: String): ChannelMessage {
        return ChannelMessage(
            getMessageTextDescription(feature, configId),
            getMessageHtmlDescription(feature, configId),
            null
        )
    }

    fun generateEventSource(feature: Feature, configId: String): EventSource {
        return EventSource(
            getMessageTitle(feature, configId),
            configId,
            feature,
            SeverityType.INFO
        )
    }

    private fun getMessageTitle(feature: Feature, configId: String): String {
        return "[$feature] Test Message Title-$configId" // TODO: change as spec
    }

    private fun getMessageTextDescription(feature: Feature, configId: String): String {
        return "Test message content body for config id $configId\nfrom feature ${feature.tag}" // TODO: change as spec
    }

    private fun getMessageHtmlDescription(feature: Feature, configId: String): String {
        return """
            <html>
            <header><title>Test Message</title></header>
            <body>
            <p>Test Message for config id $configId from feature ${feature.tag}</p>
            </body>
            </html>
        """.trimIndent() // TODO: change as spec
    }
}
