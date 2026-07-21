/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import com.google.gson.JsonObject
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.core.rest.RestStatus.OK
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.SLACK_URL
import org.opensearch.notifications.createSlackNotificationConfig
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET

/**
 * Integration tests for `POST /_plugins/_notifications/configs/_reencrypt`.
 */
class ReencryptNotificationConfigsIT : PluginRestTestCase() {

    /**
     * Happy path: configs created in the current session are already encrypted with the active key.
     * Calling `_reencrypt` should skip all of them, migrate none, fail none, and leave no remaining work.
     * After the call, all configs must still be readable and intact.
     */
    fun `test reencrypt skips configs already encrypted with the active key`() {
        val url = "https://hooks.slack.com/services/reencrypt_test_url_1"

        val config1 = createSlackNotificationConfig(SLACK_URL)
        val config2 = createSlackNotificationConfig(url)

        val configId1 = createConfigWithRequestJsonString(createJsonFromConfig(config1))
        val configId2 = createConfigWithRequestJsonString(createJsonFromConfig(config2))
        assertNotNull(configId1)
        assertNotNull(configId2)

        val reencryptJsonResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs/_reencrypt",
            "",
            OK.status
        )
        ReencryptResponse.fromJsonResponse(reencryptJsonResponse).also {
            assertEquals("No configs should require migration", 0, it.migrated)
            assertTrue("Both freshly-created configs should be skipped", it.skipped >= 2)
            assertEquals("No configs should have failed", 0, it.failed)
            assertEquals("remaining must be 0 after a clean run", 0, it.remaining)
        }

        // Both configs were just created with the active key -> they should be skipped, not migrated.
        val configResponse1 = executeRequest(
            GET.name,
            "$PLUGIN_BASE_URI/configs/$configId1",
            "",
            OK.status
        )
        verifySingleConfigEquals(configId1, config1, configResponse1)

        val configResponse2 = executeRequest(
            GET.name,
            "$PLUGIN_BASE_URI/configs/$configId2",
            "",
            OK.status
        )
        verifySingleConfigEquals(configId2, config2, configResponse2)
    }

    fun createJsonFromConfig(config: NotificationConfig): String {
        return """
            {
                "config": {
                    "name": "${config.name}",
                    "description": "${config.description}",
                    "config_type": "slack",
                    "is_enabled": ${config.isEnabled},
                    "slack": {"url": "${(config.configData as Slack).url}"}
                }
            }
        """.trimIndent()
    }

    data class ReencryptResponse(
        val migrated: Int,
        val skipped: Int,
        val failed: Int,
        val remaining: Int
    ) {
        companion object {
            fun fromJsonResponse(jsonResponse: JsonObject): ReencryptResponse {
                val migrated = jsonResponse.get("migrated").asInt
                val skipped = jsonResponse.get("skipped").asInt
                val failed = jsonResponse.get("failed").asInt
                val remaining = jsonResponse.get("remaining").asInt

                return ReencryptResponse(migrated, skipped, failed, remaining)
            }
        }
    }
}
