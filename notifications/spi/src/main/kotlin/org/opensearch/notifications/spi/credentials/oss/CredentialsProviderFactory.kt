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

package org.opensearch.notifications.spi.credentials.oss

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import org.opensearch.notifications.spi.credentials.CredentialsProvider
import org.opensearch.notifications.spi.model.destination.SNSDestination

class CredentialsProviderFactory : CredentialsProvider {
    override fun getCredentialsProvider(destination: SNSDestination): AWSCredentialsProvider {
        return if (destination.roleArn != null) {
            getCredentialsProviderByIAMRole(destination)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }

    private fun getCredentialsProviderByIAMRole(destination: SNSDestination): AWSCredentialsProvider {
        // TODO cache credentials by role ARN?
        val stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(ProfileCredentialsProvider())
            .withRegion(destination.getRegion())
            .build()
        val roleRequest = AssumeRoleRequest()
            .withRoleArn(destination.roleArn)
            .withRoleSessionName("opensearch-notifications")
        val roleResponse = stsClient.assumeRole(roleRequest)
        val sessionCredentials = roleResponse.credentials
        val awsCredentials = BasicSessionCredentials(
            sessionCredentials.accessKeyId,
            sessionCredentials.secretAccessKey,
            sessionCredentials.sessionToken
        )
        return AWSStaticCredentialsProvider(awsCredentials)
    }
}
