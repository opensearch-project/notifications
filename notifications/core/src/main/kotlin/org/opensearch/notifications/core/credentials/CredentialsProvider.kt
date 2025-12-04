/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.credentials

import com.amazonaws.auth.AWSCredentialsProvider

/**
 * AWS Credential provider using region and/or role
 */
interface CredentialsProvider {
    /**
     * create/get AWS Credential provider using region and/or role
     * @param region AWS region
     * @param roleArn optional role ARN
     * @return AWSCredentialsProvider
     */
    fun getCredentialsProvider(
        region: String,
        roleArn: String?,
    ): AWSCredentialsProvider
}
