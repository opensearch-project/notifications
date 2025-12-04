/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.bwc

import org.junit.Assert
import org.opensearch.common.settings.Settings
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.rest.RestRequest

class NotificationsBackwardsCompatibilityIT : PluginRestTestCase() {
    companion object {
        private val CLUSTER_TYPE = ClusterType.parse(System.getProperty("tests.rest.bwcsuite"))
        private val CLUSTER_NAME = System.getProperty("tests.clustername")
    }

    override fun preserveIndicesUponCompletion(): Boolean = true

    override fun preservePluginIndicesAfterTest(): Boolean = true

    override fun restClientSettings(): Settings =
        Settings
            .builder()
            .put(super.restClientSettings())
            // increase the timeout here to 90 seconds to handle long waits for a green
            // cluster health. the waits for green need to be longer than a minute to
            // account for delayed shards
            .put(CLIENT_SOCKET_TIMEOUT, "90s")
            .build()

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    fun `test backwards compatibility`() {
        val uri = getPluginUri()
        val responseMap = getAsMap(uri)["nodes"] as Map<String, Map<String, Any>>
        val configId = randomAlphaOfLength(20)
        for (response in responseMap.values) {
            val plugins = response["plugins"] as List<Map<String, Any>>
            val pluginNames = plugins.map { plugin -> plugin["name"] }.toSet()
            when (CLUSTER_TYPE) {
                ClusterType.OLD -> {
                    assertTrue(pluginNames.contains("opensearch-notifications-core"))
                    assertTrue(pluginNames.contains("opensearch-notifications"))
                    createTestNotificationsConfig(configId)
                }

                ClusterType.MIXED -> {
                    verifyConfigsExist(setOf(configId))
                }

                ClusterType.UPGRADED -> {
                    verifyConfigsExist(setOf(configId))
                    createTestNotificationsConfig(randomAlphaOfLength(10))
                    Assert.assertEquals(2, getCurrentMappingsSchemaVersion())
                }
            }
            break
        }
    }

    private enum class ClusterType {
        OLD,
        MIXED,
        UPGRADED,
        ;

        companion object {
            fun parse(value: String): ClusterType =
                when (value) {
                    "old_cluster" -> OLD
                    "mixed_cluster" -> MIXED
                    "upgraded_cluster" -> UPGRADED
                    else -> throw AssertionError("Unknown cluster type: $value")
                }
        }
    }

    private fun getPluginUri(): String =
        when (CLUSTER_TYPE) {
            ClusterType.OLD -> {
                "_nodes/$CLUSTER_NAME-0/plugins"
            }

            ClusterType.MIXED -> {
                when (System.getProperty("tests.rest.bwcsuite_round")) {
                    "second" -> "_nodes/$CLUSTER_NAME-1/plugins"
                    "third" -> "_nodes/$CLUSTER_NAME-2/plugins"
                    else -> "_nodes/$CLUSTER_NAME-0/plugins"
                }
            }

            ClusterType.UPGRADED -> {
                "_nodes/plugins"
            }
        }

    // TODO: Add a utility method to create random config types instead of just Slack.
    //   This should be generally accessible to all integ tests.
    private fun createTestNotificationsConfig(configId: String) {
        val requestJsonString =
            """
            {
                "config_id": "$configId",
                "config": {
                    "name": "This is a sample config name $configId",
                    "description": "This is a sample config description $configId",
                    "config_type": "slack",
                    "is_enabled": true,
                    "slack": { "url": "https://hooks.slack.com/services/sample_slack_url#$configId" }
                }
            }
            """.trimIndent()
        val createdConfigId = createConfigWithRequestJsonString(requestJsonString)
        assertNotNull(createdConfigId)
        Thread.sleep(100)
    }

    private fun verifyConfigsExist(idSet: Set<String>) {
        val getConfigsResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
                "",
                RestStatus.OK.status,
            )
        val configList = getConfigsResponse.get("config_list").asJsonArray
        assertEquals("Expected ${idSet.size} configs but found configList.size()", idSet.size, configList.size())
        configList.forEach {
            val item = it.asJsonObject
            val configId = item.get("config_id").asString
            assertNotNull(configId)
            assertTrue(idSet.contains(configId))
        }
    }
}
