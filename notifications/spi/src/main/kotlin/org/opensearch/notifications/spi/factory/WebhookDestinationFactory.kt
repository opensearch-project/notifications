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

package org.opensearch.notifications.spi.factory

import org.opensearch.notifications.spi.client.DestinationHttpClient
import org.opensearch.notifications.spi.client.DestinationHttpClientPool
import org.opensearch.notifications.spi.model.ChannelMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.WebhookDestination
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to Chime destination.
 */
internal class WebhookDestinationFactory : DestinationFactory<WebhookDestination> {

    private val log by logger(WebhookDestinationFactory::class.java)
    var destinationHttpClient: DestinationHttpClient = DestinationHttpClientPool.httpClient

    override fun sendMessage(destination: WebhookDestination, message: MessageContent): ChannelMessageResponse {
        return try {
            val response = destinationHttpClient.execute(destination, message)
            ChannelMessageResponse(
                recipient = destination.destinationType,
                statusCode = RestStatus.OK,
                statusText = response
            )
        } catch (exception: IOException) {
            log.error("Exception sending message: $message", exception)
            ChannelMessageResponse(
                recipient = destination.destinationType,
                statusCode = RestStatus.INTERNAL_SERVER_ERROR,
                statusText = "Failed to send message"
            )
        }
    }
}
