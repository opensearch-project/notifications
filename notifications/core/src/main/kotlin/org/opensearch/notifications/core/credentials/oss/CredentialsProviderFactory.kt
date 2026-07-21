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

    override fun getCredentialsProvider(region: String, roleArn: String?, sessionPolicy: String?, applicationId: String?, roleSessionName: String?): AWSCredentialsProvider {
        return if (roleArn != null) {
            getCredentialsProviderByIAMRole(region, roleArn, sessionPolicy, roleSessionName)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }

    private fun getCredentialsProviderByIAMRole(region: String, roleArn: String?, sessionPolicy: String?, roleSessionName: String?): AWSCredentialsProvider {
        val stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
            .withRegion(region)
            .build()
        val roleRequest = AssumeRoleRequest()
            .withRoleArn(roleArn)
            .withRoleSessionName(sanitizeSessionName(roleSessionName))
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

    companion object {
        private const val DEFAULT_SESSION_NAME = "opensearch-notifications"

        // STS role session names must be 2-64 chars matching [\w+=,.@-].
        private const val MAX_SESSION_NAME_LENGTH = 64
        private val DISALLOWED_SESSION_NAME_CHARS = Regex("[^\\w+=,.@-]")

        /**
         * Produce a valid STS role session name. Falls back to [DEFAULT_SESSION_NAME] when the
         * requested name is null/blank, replaces disallowed characters, and truncates to the
         * STS 64-character limit.
         */
        internal fun sanitizeSessionName(requested: String?): String {
            if (requested.isNullOrBlank()) {
                return DEFAULT_SESSION_NAME
            }
            val cleaned = DISALLOWED_SESSION_NAME_CHARS.replace(requested, "-")
            return if (cleaned.length <= MAX_SESSION_NAME_LENGTH) cleaned else cleaned.substring(0, MAX_SESSION_NAME_LENGTH)
        }
    }
}
