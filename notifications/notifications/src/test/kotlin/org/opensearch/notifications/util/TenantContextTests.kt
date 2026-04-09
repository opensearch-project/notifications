/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class TenantContextTests {

    @Test
    fun `test currentTenantId returns tenant id when set`() {
        runBlocking(TenantContext("test-tenant")) {
            assertEquals("test-tenant", currentTenantId())
        }
    }

    @Test
    fun `test currentTenantId returns null for single tenant deployment`() {
        runBlocking {
            assertNull(currentTenantId())
        }
    }

    @Test
    fun `test currentTenantId returns null when tenant id header is absent`() {
        runBlocking(TenantContext(null)) {
            assertNull(currentTenantId())
        }
    }

    @Test
    fun `test tenant id propagates to nested operations`() {
        runBlocking(TenantContext("parent-tenant")) {
            launch {
                assertEquals("parent-tenant", currentTenantId())
            }
        }
    }

    @Test
    fun `test concurrent requests have isolated tenant ids`() {
        runBlocking {
            launch(TenantContext("tenant-a")) {
                assertEquals("tenant-a", currentTenantId())
            }
            launch(TenantContext("tenant-b")) {
                assertEquals("tenant-b", currentTenantId())
            }
        }
    }
}
