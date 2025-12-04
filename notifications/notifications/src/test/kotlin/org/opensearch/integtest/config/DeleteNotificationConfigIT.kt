/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifyMultiConfigIdEquals
import org.opensearch.notifications.verifySingleConfigIdEquals
import org.opensearch.rest.RestRequest

class DeleteNotificationConfigIT : PluginRestTestCase() {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun `test Delete single notification config`() {
        val configId = createConfig()
        Thread.sleep(1000)

        // Get notification config by id
        val getConfigResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs/$configId",
                "",
                RestStatus.OK.status,
            )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals(
            "OK",
            deleteResponse
                .get("delete_response_list")
                .asJsonObject
                .get(configId)
                .asString,
        )
        Thread.sleep(100)

        // Get notification config after delete
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status,
        )
        Thread.sleep(100)
    }

    fun `test Delete single absent notification config should fail`() {
        val configId = createConfig()
        Thread.sleep(1000)

        // Get notification config by id
        val getConfigResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs/$configId",
                "",
                RestStatus.OK.status,
            )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/${configId}extra",
            "",
            RestStatus.NOT_FOUND.status,
        )
    }

    fun `test Delete multiple notification config`() {
        val configIds: Set<String> = (1..20).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        val deleteResponse = deleteConfigs(configIds)
        val deletedObject = deleteResponse.get("delete_response_list").asJsonObject
        configIds.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(1000)

        // Get notification configs after delete
        val getAfterDelete =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        Assert.assertEquals(0, getAfterDelete.get("total_hits").asInt)
        Thread.sleep(100)
    }

    fun `test Delete some items from multiple notification config with missing configs should fail`() {
        val configIds: Set<String> = (1..19).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse)
        Thread.sleep(100)

        var index = 0
        val partitions = configIds.partition { (index++) % 2 == 0 }
        val deletedIds = partitions.first.toSet()

        // Delete notification config
        executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs?config_id_list=${deletedIds.joinToString(separator = ",")},extra_id",
            "",
            RestStatus.NOT_FOUND.status,
        )
        Thread.sleep(1000)

        // Get notification configs after failed delete
        val getAfterDelete =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        verifyMultiConfigIdEquals(configIds, getAfterDelete)
        Thread.sleep(100)
    }

    fun `test Delete partial items from multiple notification config`() {
        val configIds: Set<String> = (1..19).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse)
        Thread.sleep(100)

        var index = 0
        val partitions = configIds.partition { (index++) % 2 == 0 }
        val deletedIds = partitions.first.toSet()
        val remainingIds = partitions.second.toSet()

        // Delete notification config
        val deleteResponse = deleteConfigs(deletedIds)
        val deletedObject = deleteResponse.get("delete_response_list").asJsonObject
        deletedIds.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(1000)

        // Get notification configs after delete
        val getAfterDelete =
            executeRequest(
                RestRequest.Method.GET.name,
                "$PLUGIN_BASE_URI/configs",
                "",
                RestStatus.OK.status,
            )
        verifyMultiConfigIdEquals(remainingIds, getAfterDelete)
        Thread.sleep(100)
    }
}
