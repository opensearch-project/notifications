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
