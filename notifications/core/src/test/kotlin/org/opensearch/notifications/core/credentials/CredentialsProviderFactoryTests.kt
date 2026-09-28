/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.core.credentials

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.credentials.oss.CredentialsProviderFactory

internal class CredentialsProviderFactoryTests {

    @Test
    fun `sanitizeSessionName falls back to default when null`() {
        assertEquals("opensearch-notifications", CredentialsProviderFactory.sanitizeSessionName(null))
    }

    @Test
    fun `sanitizeSessionName falls back to default when blank`() {
        assertEquals("opensearch-notifications", CredentialsProviderFactory.sanitizeSessionName("   "))
    }

    @Test
    fun `sanitizeSessionName preserves a valid config-scoped name`() {
        val name = "alerting-notification-3fa85f64-5717-4562-b3fc-2c963f66afa6"
        assertEquals(name, CredentialsProviderFactory.sanitizeSessionName(name))
    }

    @Test
    fun `sanitizeSessionName replaces characters not allowed by STS`() {
        assertEquals("a-b-c-d", CredentialsProviderFactory.sanitizeSessionName("a/b c:d"))
    }

    @Test
    fun `sanitizeSessionName truncates to the STS 64 character limit`() {
        val result = CredentialsProviderFactory.sanitizeSessionName("a".repeat(100))
        assertEquals(64, result.length)
        assertTrue(result.all { it == 'a' })
    }
}
