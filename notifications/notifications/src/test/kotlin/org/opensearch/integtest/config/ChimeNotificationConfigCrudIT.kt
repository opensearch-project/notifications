/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class ChimeNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete chime notification config using REST client`() {
        // Create sample config request reference
        val sampleChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${(referenceObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get chime notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        Thread.sleep(100)

        // Get all notification config

        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getAllConfigResponse)
        Thread.sleep(100)

        // Updated notification config object
        val updatedChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=654321")
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = updatedChime
        )

        // Update chime notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"chime",
                "is_enabled":${updatedObject.isEnabled},
                "chime":{"url":"${(updatedObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        val updateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            updateRequestJsonString,
            RestStatus.OK.status
        )
        Assert.assertEquals(configId, updateResponse.get("config_id").asString)
        Thread.sleep(1000)

        // Get updated chime notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        Thread.sleep(100)

        // Delete chime notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(1000)

        // Get chime notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test BAD Request for multiple config data for Chime using REST Client`() {
        // Create sample config request reference
        val sampleChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"https://hooks.slack.com/services/sample_slack_url"}
                "chime":{"url":"${(referenceObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test update existing config to different config type`() {
        // Create sample config request reference
        val sampleChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${(referenceObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Update to slack notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"this is a updated config name",
                "description":"this is a updated config description",
                "config_type":"slack",
                "is_enabled":"true",
                "slack":{"url":"https://hooks.slack.com/services/sample_slack_url"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            updateRequestJsonString,
            RestStatus.CONFLICT.status
        )
    }

    fun `test BAD create request with invalid webhook URL`() {
        // Create sample config request reference
        val sampleChimeConfigData = Chime("https://")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChimeConfigData
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"http"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.INTERNAL_SERVER_ERROR.status
        )
    }

    fun `test BAD delete request on non-existent config ID`() {
        val configId = "abcdefghijk"
        executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
    }

    fun `test update Chime webhook URL`() {
        // Create sample config request reference
        val sampleChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${(referenceObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // update to new webhook URL
        val updateRequestJsonString = """
        {
            "config":{
                "name":"this is a updated config name",
                "description":"this is a updated config description",
                "config_type":"chime",
                "is_enabled":"true",
                "chime":{"url":"https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=654321"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            updateRequestJsonString,
            RestStatus.OK.status
        )
        // test BAD update with invalid webhook URL
        val badUpdateRequestJsonString = """
        {
            "config":{
                "name":"this is a updated config name",
                "description":"this is a updated config description",
                "config_type":"chime",
                "is_enabled":"true",
                "chime":{"url":"http"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            badUpdateRequestJsonString,
            RestStatus.INTERNAL_SERVER_ERROR.status
        )
    }
}
