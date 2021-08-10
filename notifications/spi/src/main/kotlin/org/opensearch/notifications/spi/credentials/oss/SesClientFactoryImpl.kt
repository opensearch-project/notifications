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

import org.opensearch.notifications.spi.credentials.SesClientFactory
import org.opensearch.notifications.spi.utils.SecurityAccess
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient

/**
 * Factory for creating SES client
 */
object SesClientFactoryImpl : SesClientFactory {
    override fun createSesClient(region: Region, roleArn: String?): SesClient {
        return SecurityAccess.doPrivileged {
            // TODO: use CredentialsProviderFactory when it supports AWS SDK v2
            SesClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()
        }
    }
}
