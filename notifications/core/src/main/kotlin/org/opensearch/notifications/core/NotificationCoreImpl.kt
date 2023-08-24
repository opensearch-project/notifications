/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core

import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.spi.NotificationCore
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.BaseDestination
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * This is a client facing NotificationCoreImpl class to send the messages
 * to the NotificationCoreImpl channels like chime, slack, Microsoft Teams, webhooks, email etc
 */
object NotificationCoreImpl : NotificationCore {
    /**
     * Send the notification message to the corresponding destination
     *
     * @param destination destination configuration for sending message
     * @param message metadata for message
     * @param referenceId referenceId for message
     * @return ChannelMessageResponse
     */
    override fun sendMessage(
        destination: BaseDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        return AccessController.doPrivileged(
            PrivilegedAction {
                val destinationFactory = DestinationTransportProvider.getTransport(destination.destinationType)
                destinationFactory.sendMessage(destination, message, referenceId)
            } as PrivilegedAction<DestinationMessageResponse>?
        )
    }

    /**
     * Get list of allowed destinations
     */
    override fun getAllowedConfigTypes(): List<String> {
        return AccessController.doPrivileged(
            PrivilegedAction {
                PluginSettings.allowedConfigTypes
            } as PrivilegedAction<List<String>>?
        )
    }

    /**
     * Get map of plugin features
     */
    override fun getPluginFeatures(): Map<String, String> {
        return AccessController.doPrivileged(
            PrivilegedAction {
                mapOf(
                    Pair("tooltip_support", "${PluginSettings.tooltipSupport}")
                )
            } as PrivilegedAction<Map<String, String>>?
        )
    }
}
