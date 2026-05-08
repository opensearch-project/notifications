/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.credentials.oss

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import org.opensearch.notifications.core.credentials.CredentialsProvider

class CredentialsProviderFactory : CredentialsProvider {

    override fun getCredentialsProvider(region: String, roleArn: String?, sessionPolicy: String?, applicationId: String?): AWSCredentialsProvider {
        return if (roleArn != null) {
            getCredentialsProviderByIAMRole(region, roleArn, sessionPolicy)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }

    private fun getCredentialsProviderByIAMRole(region: String, roleArn: String?, sessionPolicy: String?): AWSCredentialsProvider {
        val stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
            .withRegion(region)
            .build()
        val roleRequest = AssumeRoleRequest()
            .withRoleArn(roleArn)
            .withRoleSessionName("opensearch-notifications")
        if (sessionPolicy != null) {
            roleRequest.withPolicy(sessionPolicy)
        }
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
