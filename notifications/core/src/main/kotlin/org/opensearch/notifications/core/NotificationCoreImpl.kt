/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
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
 * to the NotificationCoreImpl channels like chime, slack, webhooks, email etc
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
     * Get list of allowed config features
     */
    override fun getAllowedConfigFeatures(): List<String> {
        return AccessController.doPrivileged(
            PrivilegedAction {
                PluginSettings.allowedConfigFeatures
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
