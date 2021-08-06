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

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.opensearch.notifications.spi.credentials.SNSClient
import org.opensearch.notifications.spi.model.destination.SNSDestination

class SNSClientFactory : SNSClient {
    override fun getClient(destination: SNSDestination): AmazonSNS {
        val credentials = CredentialsProviderFactory().getCredentialsProvider(destination)
        return AmazonSNSClientBuilder.standard()
            .withRegion(destination.getRegion())
            .withCredentials(credentials)
            .build()
    }
}
