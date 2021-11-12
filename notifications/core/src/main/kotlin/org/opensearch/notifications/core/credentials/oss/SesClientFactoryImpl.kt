/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.core.credentials.oss

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import org.opensearch.notifications.core.credentials.SesClientFactory
import org.opensearch.notifications.core.utils.SecurityAccess

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
