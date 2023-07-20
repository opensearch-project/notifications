/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.transport

import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.client.DestinationClientPool
import org.opensearch.notifications.core.client.DestinationHttpClient
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.WebhookDestination
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to all types of webhook destinations.
 */
internal class WebhookDestinationTransport : DestinationTransport<WebhookDestination> {

    private val log by logger(WebhookDestinationTransport::class.java)
    private val destinationHttpClient: DestinationHttpClient

    constructor() {
        this.destinationHttpClient = DestinationClientPool.httpClient
    }

    @OpenForTesting
    constructor(destinationHttpClient: DestinationHttpClient) {
        this.destinationHttpClient = destinationHttpClient
    }

    override fun sendMessage(
        destination: WebhookDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        return try {
            val response = destinationHttpClient.execute(destination, message, referenceId)
            DestinationMessageResponse(RestStatus.OK.status, response)
        } catch (exception: IOException) {
            log.error("Exception sending webhook message $referenceId: $message", exception)
            DestinationMessageResponse(
                RestStatus.INTERNAL_SERVER_ERROR.status,
                "Failed to send webhook message ${exception.message}"
            )
        } catch (illegalArgumentException: IllegalArgumentException) {
            log.error(
                "Exception sending webhook message: message creation failed with status:${illegalArgumentException.message}"
            )
            DestinationMessageResponse(
                RestStatus.BAD_REQUEST.status,
                "Webhook message creation failed with status:${illegalArgumentException.message}"
            )
        }
    }
}
