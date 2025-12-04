/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.core.credentials

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService

/**
 * Interface for creating SES client
 */
interface SesClientFactory {
    fun createSesClient(
        region: String,
        roleArn: String?,
    ): AmazonSimpleEmailService
}
