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
import org.opensearch.notifications.spi.credentials.oss.SNSClientFactory
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SNSDestination

/**
 * This class handles the SNS connections to the given Destination.
 */
class DestinationSNSClient(destination: SNSDestination) {

    private val amazonSNS: AmazonSNS = SNSClientFactory().getClient(destination)

    fun execute(topicArn: String, message: MessageContent): String {
        val result = amazonSNS.publish(topicArn, message.textDescription, message.title)
        return result.messageId // TODO: return what?
    }
}
