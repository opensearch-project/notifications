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
     * @param sessionPolicy optional inline session policy JSON to scope down permissions
     * @param roleSessionName optional STS role session name (for CloudTrail attribution)
     * @return AWSCredentialsProvider
     */
    fun getCredentialsProvider(region: String, roleArn: String?, sessionPolicy: String? = null, applicationId: String? = null, roleSessionName: String? = null): AWSCredentialsProvider
}
