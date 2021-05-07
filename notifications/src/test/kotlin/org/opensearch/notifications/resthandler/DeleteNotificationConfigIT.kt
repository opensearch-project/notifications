/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.notifications.resthandler

import org.junit.Assert
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.PluginRestTestCase
import org.opensearch.notifications.verifyMultiConfigIdEquals
import org.opensearch.notifications.verifySingleConfigIdEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import kotlin.random.Random

class DeleteNotificationConfigIT : PluginRestTestCase() {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun getCreateRequestJsonString(): String {
        val randomString = (1..20)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
        return """
        {
            "config_id":"$randomString",
            "notification_config":{
                "name":"this is a sample config name $randomString",
                "description":"this is a sample config description $randomString",
                "config_type":"slack",
                "feature_list":[
                    "index_management",
                    "reports"
                ],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#$randomString"}
            }
        }
        """.trimIndent()
    }

    private fun createConfig(): String {
        val createRequestJsonString = getCreateRequestJsonString()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(100)
        return configId
    }

    fun `test Delete single notification config`() {
        val configId = createConfig()
        Thread.sleep(1000)

        // Get notification config by id
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(100)

        // Get notification config after delete
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Delete single absent notification config should fail`() {
        val configId = createConfig()
        Thread.sleep(1000)

        // Get notification config by id
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/${configId}extra",
            "",
            RestStatus.NOT_FOUND.status
        )
    }

    fun `test Delete multiple notification config`() {
        val configIds: Set<String> = (1..20).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse)
        Thread.sleep(100)

        // Delete notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs?config_id_list=${configIds.joinToString(separator = ",")}",
            "",
            RestStatus.OK.status
        )
        val deletedObject = deleteResponse.get("delete_response_list").asJsonObject
        configIds.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(1000)

        // Get notification configs after delete
        val getAfterDelete = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(0, getAfterDelete.get("total_hits").asInt)
        Thread.sleep(100)
    }

    fun `test Delete some items from multiple notification config with missing configs should fail`() {
        val configIds: Set<String> = (1..19).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
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
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(1000)

        // Get notification configs after failed delete
        val getAfterDelete = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getAfterDelete)
        Thread.sleep(100)
    }

    fun `test Delete partial items from multiple notification config`() {
        val configIds: Set<String> = (1..19).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse)
        Thread.sleep(100)

        var index = 0
        val partitions = configIds.partition { (index++) % 2 == 0 }
        val deletedIds = partitions.first.toSet()
        val remainingIds = partitions.second.toSet()

        // Delete notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs?config_id_list=${deletedIds.joinToString(separator = ",")}",
            "",
            RestStatus.OK.status
        )
        val deletedObject = deleteResponse.get("delete_response_list").asJsonObject
        deletedIds.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(1000)

        // Get notification configs after delete
        val getAfterDelete = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(remainingIds, getAfterDelete)
        Thread.sleep(100)
    }
}
