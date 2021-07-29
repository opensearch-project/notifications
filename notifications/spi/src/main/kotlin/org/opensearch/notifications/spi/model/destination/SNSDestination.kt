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
package org.opensearch.notifications.spi.model.destination

import org.opensearch.common.settings.SecureString

/**
 * This class holds the contents of SNS destination
 */
data class SNSDestination(
    val topicArn: String,
    val roleArn: String? = null,
    val accessKey: SecureString? = null,
    val secretKey: SecureString? = null
) : BaseDestination(DestinationType.SNS) {

    /**
     * Check if IAM credential is configured.
     */
    fun isIAMCredentialConfigured(): Boolean {
        return (accessKey != null && secretKey != null)
    }
}
