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
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
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
        val result = amazonSNS.publish(topicArn, message.textDescription, message.title)
        return result.messageId // TODO: return what?
    }

    private fun getCredentialProvider(destination: SNSDestination): AWSCredentialsProvider {
        return when {
            destination.isIAMCredentialConfigured() -> {
                val awsCredentials = BasicAWSCredentials(
                    destination.accessKey.toString(),
                    destination.secretKey.toString()
                )
                AWSStaticCredentialsProvider(awsCredentials)
            }
            destination.roleArn != null -> {
                val stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(ProfileCredentialsProvider()) // TODO confirm if this is needed?
                    .withRegion(getRegion((destination.topicArn)))
                    .build()
                val roleRequest = AssumeRoleRequest()
                    .withRoleArn(destination.roleArn)
                    .withRoleSessionName("NotificationsTempSession")
                val roleResponse = stsClient.assumeRole(roleRequest)
                val sessionCredentials = roleResponse.credentials
                val awsCredentials = BasicSessionCredentials(
                    sessionCredentials.accessKeyId,
                    sessionCredentials.secretAccessKey,
                    sessionCredentials.sessionToken
                )
                AWSStaticCredentialsProvider(awsCredentials)
            }
            else -> {
                DefaultAWSCredentialsProviderChain()
            }
        }
    }

    private fun getRegion(arn: String): String {
        // sample topic arn arn:aws:sns:us-west-2:075315751589:test-notification
        return arn.split(":".toRegex()).toTypedArray()[3]
    }
}
