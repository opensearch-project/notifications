/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.core.credentials

import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.credentials.oss.SnsClientFactoryImpl

/**
 * SNS counterpart to SesClientFactoryImplTests. SnsClientFactoryImpl.createSnsClient
 * goes through the same CredentialsProviderFactory / AmazonWebServiceClient construction
 * path as SesClientFactoryImpl, so it hit the same two NoClassDefFoundErrors (classic
 * jackson-databind, then classic httpclient/httpcore) before core/build.gradle was fixed.
 */
internal class SnsClientFactoryImplTests {

    @Test
    fun `test createSnsClient does not throw NoClassDefFoundError for jackson databind or httpclient`() {
        SnsClientFactoryImpl.createSnsClient("us-west-2", null)
    }
}
