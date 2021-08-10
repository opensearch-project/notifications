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

package org.opensearch.notifications.spi.client

import com.amazonaws.services.sns.AmazonSNS
import org.opensearch.notifications.spi.credentials.SnsClientFactory
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SnsDestination

/**
 * This class handles the SNS connections to the given Destination.
 */
class DestinationSnsClient(private val snsClientFactory: SnsClientFactory) {

    fun execute(destination: SnsDestination, message: MessageContent, referenceId: String): String {
        val amazonSNS: AmazonSNS = snsClientFactory.createSnsClient(destination.region, destination.roleArn)
        val result = amazonSNS.publish(destination.topicArn, message.textDescription, message.title)
        return result.messageId
    }
}
