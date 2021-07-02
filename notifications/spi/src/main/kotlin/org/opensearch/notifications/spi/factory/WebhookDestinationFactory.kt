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

import org.opensearch.notifications.spi.client.DestinationClientPool
import org.opensearch.notifications.spi.client.DestinationHttpClient
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.WebhookDestination
import org.opensearch.notifications.spi.utils.OpenForTesting
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to all types of webhook destinations.
 */
internal class WebhookDestinationFactory : DestinationFactory<WebhookDestination> {

    private val log by logger(WebhookDestinationFactory::class.java)
    private val destinationHttpClient: DestinationHttpClient

    constructor() {
        this.destinationHttpClient = DestinationClientPool.httpClient
    }

    @OpenForTesting
    constructor(destinationHttpClient: DestinationHttpClient) {
        this.destinationHttpClient = destinationHttpClient
    }

    override fun sendMessage(destination: WebhookDestination, message: MessageContent): DestinationMessageResponse {
        return try {
            val response = destinationHttpClient.execute(destination, message)
            DestinationMessageResponse(RestStatus.OK, response)
        } catch (exception: IOException) {
            log.error("Exception sending message: $message", exception)
            DestinationMessageResponse(RestStatus.INTERNAL_SERVER_ERROR, "Failed to send message ${exception.message}")
        }
    }
}
