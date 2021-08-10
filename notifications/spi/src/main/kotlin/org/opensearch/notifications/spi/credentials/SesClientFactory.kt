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
package org.opensearch.notifications.spi.credentials

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient

/**
 * Interface for creating SES client
 */
interface SesClientFactory {
    fun createSesClient(region: Region, roleArn: String?): SesClient
}
