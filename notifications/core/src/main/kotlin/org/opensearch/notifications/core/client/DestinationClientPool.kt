/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

import org.opensearch.notifications.core.credentials.oss.SesClientFactoryImpl
import org.opensearch.notifications.core.credentials.oss.SnsClientFactoryImpl

/**
 * This class provides Client to the relevant destinations
 */
internal object DestinationClientPool {
    val httpClient: DestinationHttpClient = DestinationHttpClient()
    val smtpClient: DestinationSmtpClient = DestinationSmtpClient()
    val snsClient: DestinationSnsClient = DestinationSnsClient(SnsClientFactoryImpl)
    val sesClient: DestinationSesClient = DestinationSesClient(SesClientFactoryImpl)
}
