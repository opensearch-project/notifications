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
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SNSDestination
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to SNS destinations.
 */
internal class SNSDestinationFactory : DestinationFactory<SNSDestination> {

    private val log by logger(SNSDestinationFactory::class.java)

    override fun sendMessage(destination: SNSDestination, message: MessageContent): DestinationMessageResponse {
        return try {
            val snsClient = DestinationClientPool.getSNSClient(destination)
            val response = snsClient.execute(destination.topicArn, message)
            DestinationMessageResponse(RestStatus.OK.status, response)
        } catch (exception: IOException) {
            log.error("Exception sending message: $message", exception)
            DestinationMessageResponse(RestStatus.INTERNAL_SERVER_ERROR.status, "Failed to send message ${exception.message}")
        }
    }
}
