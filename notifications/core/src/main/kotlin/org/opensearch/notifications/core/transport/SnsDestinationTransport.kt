/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.transport

import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.client.DestinationClientPool
import org.opensearch.notifications.core.client.DestinationSnsClient
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SnsDestination
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
        referenceId: String,
    ): DestinationMessageResponse =
        try {
            destinationSNSClient.execute(destination, message, referenceId)
        } catch (exception: IOException) {
            log.error("Exception sending message id $referenceId", exception)
            DestinationMessageResponse(
                RestStatus.INTERNAL_SERVER_ERROR.status,
                "Failed to send SNS message ${exception.message}",
            )
        }
}
