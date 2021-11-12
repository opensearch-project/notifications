/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.credentials.oss

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.opensearch.notifications.core.credentials.SnsClientFactory
import org.opensearch.notifications.core.utils.SecurityAccess

/**
 * Factory for creating SNS client
 */
object SnsClientFactoryImpl : SnsClientFactory {
    override fun createSnsClient(region: String, roleArn: String?): AmazonSNS {
        return SecurityAccess.doPrivileged {
            val credentials =
                CredentialsProviderFactory().getCredentialsProvider(region, roleArn)
            AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentials)
                .build()
        }
    }
}
