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
import org.opensearch.notifications.core.client.DestinationSnsClient
import org.opensearch.notifications.core.spi.model.DestinationMessageResponse
import org.opensearch.notifications.core.spi.model.MessageContent
import org.opensearch.notifications.core.spi.model.destination.SnsDestination
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to SNS destinations.
 */
internal class SnsDestinationTransport : DestinationTransport<SnsDestination> {

    private val log by logger(SnsDestinationTransport::class.java)
    private val destinationSNSClient: DestinationSnsClient

    constructor() {
        this.destinationSNSClient = DestinationClientPool.snsClient
    }

    @OpenForTesting
    constructor(destinationSnsClient: DestinationSnsClient) {
        this.destinationSNSClient = destinationSnsClient
    }

    override fun sendMessage(
        destination: SnsDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        return try {
            destinationSNSClient.execute(destination, message, referenceId)
        } catch (exception: IOException) {
            log.error("Exception sending message id $referenceId", exception)
            DestinationMessageResponse(
                RestStatus.INTERNAL_SERVER_ERROR.status,
                "Failed to send SNS message ${exception.message}"
            )
        }
    }
}
