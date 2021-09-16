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

package org.opensearch.notifications.core.transport

import org.opensearch.notifications.core.client.DestinationClientPool
import org.opensearch.notifications.core.client.DestinationHttpClient
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.corespi.model.DestinationMessageResponse
import org.opensearch.notifications.corespi.model.MessageContent
import org.opensearch.notifications.corespi.model.destination.WebhookDestination
import org.opensearch.rest.RestStatus
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
            log.error("Exception sending message $referenceId: $message", exception)
            DestinationMessageResponse(
                RestStatus.INTERNAL_SERVER_ERROR.status,
                "Failed to send message ${exception.message}"
            )
        }
    }
}
