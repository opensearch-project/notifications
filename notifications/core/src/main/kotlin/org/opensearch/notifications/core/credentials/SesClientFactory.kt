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
package org.opensearch.notifications.core.credentials

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService

/**
 * Interface for creating SES client
 */
interface SesClientFactory {
    fun createSesClient(region: String, roleArn: String?): AmazonSimpleEmailService
}
