/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.core.credentials

import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.credentials.oss.SesClientFactoryImpl

/**
 * Reproduces a production incident where sending an SES email threw NoClassDefFoundError.
 * AWS SDK v1 (com.amazonaws.*) needs the classic Jackson databind and Apache HttpClient/
 * HttpCore packages, but core/build.gradle only declared their Jackson 3 / HttpClient5
 * replacements, so both classes were missing from the runtime classpath.
 */
internal class SesClientFactoryImplTests {

    @Test
    fun `test createSesClient does not throw NoClassDefFoundError for jackson databind or httpclient`() {
        SesClientFactoryImpl.createSesClient("us-west-2", null)
    }
}
