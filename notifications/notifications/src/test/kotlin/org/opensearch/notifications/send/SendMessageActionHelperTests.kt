/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.send

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SendMessageActionHelperTests {

    @Test
    fun `buildRoleSessionName returns null when config id is unavailable`() {
        // Null config id -> null, so the credentials factory applies its default session name.
        assertNull(buildRoleSessionName.invoke(SendMessageActionHelper, null as String?))
    }

    @Test
    fun `buildRoleSessionName prefixes the config id for CloudTrail attribution`() {
        val configId = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        assertEquals(
            "alerting-notification-$configId",
            buildRoleSessionName.invoke(SendMessageActionHelper, configId)
        )
    }

    companion object {
        private lateinit var buildRoleSessionName: Method

        @BeforeAll
        @JvmStatic
        fun initialize() {
            /* use reflection to get the private method, matching ConfigIndexingActionsTests */
            buildRoleSessionName = SendMessageActionHelper::class.java.getDeclaredMethod(
                "buildRoleSessionName",
                String::class.java
            )
            buildRoleSessionName.isAccessible = true
        }
    }
}
