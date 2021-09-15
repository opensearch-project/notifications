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
    fun getCredentialsProvider(region: String, roleArn: String?): AWSCredentialsProvider
}
