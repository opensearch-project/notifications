/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.index

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationConfigIndexTests {

    @Test
    fun `test get schema version`() {
        val indexMapping = mapOf(
            "_meta" to mapOf("schema_version" to 10),
            "user" to "test"
        )
        val schemaVersion = getSchemaVersionFromIndexMapping.invoke(NotificationConfigIndex, indexMapping)
        assertEquals(10, schemaVersion, "wrong schema version")
    }

    @Test
    fun `test get schema version without _meta field`() {
        val indexMapping = mapOf(
            "meta" to mapOf("schema_version" to 10),
            "user" to "test"
        )
        val schemaVersion = getSchemaVersionFromIndexMapping.invoke(NotificationConfigIndex, indexMapping)
        assertEquals(1, schemaVersion, "wrong schema version")
    }

    @Test
    fun `test get schema version without schema_version field`() {
        val indexMapping = mapOf(
            "_meta" to mapOf("schema" to 10),
            "user" to "test"
        )
        val schemaVersion = getSchemaVersionFromIndexMapping.invoke(NotificationConfigIndex, indexMapping)
        assertEquals(1, schemaVersion, "wrong schema version")
    }

    @Test
    fun `test get non number schema_version throw exception`() {
        val indexMapping = mapOf(
            "_meta" to mapOf("schema_version" to "10"),
            "user" to "test"
        )
        assertFails {
            getSchemaVersionFromIndexMapping.invoke(NotificationConfigIndex, indexMapping)
        }
    }

    @Test
    fun `test multiTenancyEnabled defaults to false`() {
        val value = multiTenancyEnabledField.getBoolean(NotificationConfigIndex)
        assertFalse(value, "multiTenancyEnabled should default to false")
    }

    @Test
    fun `test multiTenancyEnabled set to true via reflection`() {
        val originalValue = multiTenancyEnabledField.getBoolean(NotificationConfigIndex)
        try {
            multiTenancyEnabledField.setBoolean(NotificationConfigIndex, true)
            assertTrue(multiTenancyEnabledField.getBoolean(NotificationConfigIndex), "multiTenancyEnabled should be true")
        } finally {
            multiTenancyEnabledField.setBoolean(NotificationConfigIndex, originalValue)
        }
    }

    companion object {
        private lateinit var getSchemaVersionFromIndexMapping: Method
        private lateinit var multiTenancyEnabledField: Field

        @BeforeAll
        @JvmStatic
        fun initialize() {
            /* use reflection to get private method */
            getSchemaVersionFromIndexMapping = NotificationConfigIndex::class.java.getDeclaredMethod(
                "getSchemaVersionFromIndexMapping",
                Map::class.java
            )
            getSchemaVersionFromIndexMapping.isAccessible = true

            multiTenancyEnabledField = NotificationConfigIndex::class.java.getDeclaredField("multiTenancyEnabled")
            multiTenancyEnabledField.isAccessible = true
        }
    }
}
