/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.client.ResponseException
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.integtest.getResponseBody
import org.opensearch.integtest.jsonify
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class SlackNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete slack notification config using REST client`() {
        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get Slack notification config

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
        val updatedSlack = Slack("https://hooks.slack.com/services/updated_slack_url")
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = updatedSlack
        )

        // Update slack notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"slack",
                "is_enabled":${updatedObject.isEnabled},
                "slack":{"url":"${(updatedObject.configData as Slack).url}"}
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

        // Get updated Slack notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        Thread.sleep(100)

        // Delete slack notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(1000)

        // Get slack notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Bad Request for multiple config data for Slack using REST Client`() {
        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456"}
                "slack":{"url":"${(referenceObject.configData as Slack).url}
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

    fun `test create config with wrong Slack url and get error text`() {
        val sampleSlack = Slack("https://webhook.slack.com/services/sample_slack_url")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        val response = try {
            val request = Request(RestRequest.Method.POST.name, "$PLUGIN_BASE_URI/configs")
            request.setJsonEntity(createRequestJsonString)
            val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
            restOptionsBuilder.addHeader("Content-Type", "application/json")
            request.setOptions(restOptionsBuilder)
            client().performRequest(request)
            fail("Expected wrong Slack URL.")
        } catch (exception: ResponseException) {
            Assert.assertEquals(
                "Wrong Slack url. Should contain \"hooks.slack.com/services/\" or \"hooks.gov-slack.com/services/\"",
                jsonify(getResponseBody(exception.response))["error"].asJsonObject["reason"].asString
            )
        }
    }
}
