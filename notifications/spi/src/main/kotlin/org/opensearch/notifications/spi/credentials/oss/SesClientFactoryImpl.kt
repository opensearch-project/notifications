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

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import org.opensearch.notifications.spi.credentials.SesClientFactory
import org.opensearch.notifications.spi.utils.SecurityAccess

/**
 * Factory for creating SES client
 */
object SesClientFactoryImpl : SesClientFactory {
    override fun createSesClient(region: String, roleArn: String?): AmazonSimpleEmailService {
        return SecurityAccess.doPrivileged {
            val credentials =
                CredentialsProviderFactory().getCredentialsProvider(region, roleArn)
            AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentials)
                .build()
        }
    }
}
