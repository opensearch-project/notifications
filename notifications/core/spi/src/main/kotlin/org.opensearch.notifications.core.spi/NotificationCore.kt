package org.opensearch.notifications.core.spi

import org.opensearch.notifications.core.spi.model.DestinationMessageResponse
import org.opensearch.notifications.core.spi.model.MessageContent
import org.opensearch.notifications.core.spi.model.destination.BaseDestination

interface NotificationCore {
    /**
     * Send message to a destination
     */
    fun sendMessage(
        destination: BaseDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse

    /**
     * Get list of allowed config types
     */
    fun getAllowedConfigTypes(): List<String>

    /**
     * Get map of plugin features
     */
    fun getPluginFeatures(): Map<String, String>
}
