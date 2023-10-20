/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class WebhookNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete webhook notification config using REST client`() {
        // Create sample config request reference
        val sampleWebhook = Webhook(
            "https://domain.com/sample_webhook_url#1234567890",
            mapOf(Pair("User-Agent", "Mozilla/5.0"))
        )
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.WEBHOOK,
            isEnabled = true,
            configData = sampleWebhook
        )

        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"webhook",
                "is_enabled":${referenceObject.isEnabled},
                "webhook":{
                    "url":"${(referenceObject.configData as Webhook).url}",
                    "header_params":{
                        "User-Agent":"Mozilla/5.0"
                    }
                }
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        

        // Get webhook notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        

        // Get all notification config

        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getAllConfigResponse)
        

        // Updated notification config object
        val updatedWebhook = Webhook(
            "https://updated.domain.com/updated_webhook_url#0987654321",
            mapOf(Pair("key", "value"))
        )
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.WEBHOOK,
            isEnabled = true,
            configData = updatedWebhook
        )

        // Update webhook notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"webhook",
                "is_enabled":${updatedObject.isEnabled},
                "webhook":{
                    "url":"${(updatedObject.configData as Webhook).url}",
                    "header_params": {
                        "key":"value"
                    }
                }
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
        

        // Get updated webhook notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        

        // Delete webhook notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        

        // Get webhook notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        
    }

    fun `test Bad Request for multiple config for Webhook data using REST Client`() {
        // Create sample config request reference
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.WEBHOOK,
            isEnabled = true,
            configData = sampleWebhook
        )

        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"webhook",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"https://dummy.com"}
                "webhook":{"url":"${(referenceObject.configData as Webhook).url}"}
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
}
