/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.spi.transport

import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.BaseDestination

/**
 * Interface which enables to plug in multiple notification Channel Factories.
 *
 * @param <T> message object of type [{@link DestinationType}]
 */
internal interface DestinationTransport<T : BaseDestination> {
    /**
     * Sending notification message over this channel.
     *
     * @param destination destination configuration for sending message
     * @param message The message to send notification
     * @param referenceId referenceId for message
     * @return Channel message response
     */
    fun sendMessage(
        destination: T,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse
}
