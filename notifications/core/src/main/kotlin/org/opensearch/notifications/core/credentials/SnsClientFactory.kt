/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.credentials

import com.amazonaws.services.sns.AmazonSNS

/**
 * Interface for creating SNS client
 */
interface SnsClientFactory {
    fun createSnsClient(region: String, roleArn: String?): AmazonSNS
}
