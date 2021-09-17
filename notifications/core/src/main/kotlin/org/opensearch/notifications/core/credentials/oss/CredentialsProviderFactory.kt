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

package org.opensearch.notifications.core.credentials.oss

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import org.opensearch.notifications.core.credentials.CredentialsProvider

class CredentialsProviderFactory : CredentialsProvider {
    override fun getCredentialsProvider(region: String, roleArn: String?): AWSCredentialsProvider {
        return if (roleArn != null) {
            getCredentialsProviderByIAMRole(region, roleArn)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }

    private fun getCredentialsProviderByIAMRole(region: String, roleArn: String?): AWSCredentialsProvider {
        val stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(ProfileCredentialsProvider())
            .withRegion(region)
            .build()
        val roleRequest = AssumeRoleRequest()
            .withRoleArn(roleArn)
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
