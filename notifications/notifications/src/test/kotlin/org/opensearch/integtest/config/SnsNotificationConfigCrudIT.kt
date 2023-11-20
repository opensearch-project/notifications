/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Sns
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class SnsNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete SNS notification config using REST client`() {
        // Create sample config request reference
        val sampleSns = Sns("arn:aws:sns:us-west-2:012345678912:test-notification", "arn:aws:iam::012345678912:role/iam-test")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SNS,
            isEnabled = true,
            configData = sampleSns
        )

        // Create SNS notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"sns",
                "is_enabled":${referenceObject.isEnabled},
                "sns":{"topic_arn":"${(referenceObject.configData as Sns).topicArn}","role_arn":"${(referenceObject.configData as Sns).roleArn}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get SNS notification config

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
        val updatedSns = Sns("arn:aws:sns:us-west-2:012345678912:test-notification-updated", "arn:aws:iam::012345678912:role/updated-role-test")
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.SNS,
            isEnabled = true,
            configData = updatedSns
        )

        // Update SNS notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"sns",
                "is_enabled":${updatedObject.isEnabled},
                "sns":{"topic_arn":"${(updatedObject.configData as Sns).topicArn}","role_arn":"${(updatedObject.configData as Sns).roleArn}"}
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

        // Get updated SNS notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        Thread.sleep(100)

        // Delete SNS notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(1000)

        // Get SNS notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Bad Request for multiple config data for SNS using REST Client`() {
        // Create sample config request reference
        val sampleSns = Sns("arn:aws:sns:us-west-2:012345678912:test-notification", "arn:aws:iam::012345678912:role/iam-test")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SNS,
            isEnabled = true,
            configData = sampleSns
        )

        // Create SNS notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"sns",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456"}
                "sns":{"topic_arn":"${(referenceObject.configData as Sns).topicArn}","role_arn":"${(referenceObject.configData as Sns).roleArn}"}
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
