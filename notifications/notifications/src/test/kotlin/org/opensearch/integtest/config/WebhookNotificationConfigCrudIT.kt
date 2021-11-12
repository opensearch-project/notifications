/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_ALERTING
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

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
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS, FEATURE_ALERTING),
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
                "feature_list":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}",
                    "${referenceObject.features.elementAt(2)}"
                ],
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
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get webhook notification config

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
        val updatedWebhook = Webhook(
            "https://updated.domain.com/updated_webhook_url#0987654321",
            mapOf(Pair("key", "value"))
        )
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.WEBHOOK,
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS),
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
                "feature_list":[
                    "${updatedObject.features.elementAt(0)}",
                    "${updatedObject.features.elementAt(1)}"
                ],
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
        Thread.sleep(1000)

        // Get updated webhook notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        Thread.sleep(100)

        // Delete webhook notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(1000)

        // Get webhook notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Bad Request for multiple config for Webhook data using REST Client`() {
        // Create sample config request reference
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.WEBHOOK,
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS, FEATURE_ALERTING),
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
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}",
                    "${referenceObject.features.elementAt(2)}"
                ],
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
