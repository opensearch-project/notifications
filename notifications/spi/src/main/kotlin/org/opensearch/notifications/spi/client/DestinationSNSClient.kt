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

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SNSDestination

/**
 * This class handles the SNS connections to the given Destination.
 */
class DestinationSNSClient(destination: SNSDestination) {

    private val amazonSNS: AmazonSNS =
        AmazonSNSClientBuilder.standard()
            .withRegion(getRegion(destination.topicArn))
            .withCredentials(getCredentialProvider(destination))
            .build()

    fun execute(topicArn: String, message: MessageContent): String {
        val result = amazonSNS.publish(topicArn, message.textDescription, message.title) // TODO: check title is null
        return result.messageId // TODO: return what?
    }

    private fun getCredentialProvider(destination: SNSDestination): AWSCredentialsProvider {
        return if (destination.isIAMCredentialConfigured()) {
            val awsCredentials = BasicAWSCredentials(
                destination.accessKey.toString(),
                destination.secretKey.toString()
            )
            AWSStaticCredentialsProvider(awsCredentials)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }

    private fun getRegion(arn: String): String {
        // sample topic arn arn:aws:sns:us-west-2:075315751589:test-notification
        return arn.split(":".toRegex()).toTypedArray()[3]
    }
}
