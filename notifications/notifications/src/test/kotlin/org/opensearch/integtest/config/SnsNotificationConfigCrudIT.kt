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

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Sns
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class SnsNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete SNS notification config using REST client`() {
        // Create sample config request reference
        val sampleSns = Sns("arn:aws:sns:us-west-2:012345678912:test-notification", "arn:aws:iam::012345678912:role/iam-test")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SNS,
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS),
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
                "feature_list":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "sns":{"topic_arn":"${(referenceObject.configData as Sns).topicArn}","role_arn":"${(referenceObject.configData as Sns).roleArn}"}
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
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS),
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
                "feature_list":[
                    "${updatedObject.features.elementAt(0)}",
                    "${updatedObject.features.elementAt(1)}"
                ],
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
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
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
            setOf(FEATURE_INDEX_MANAGEMENT, FEATURE_REPORTS),
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
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"https://dummy.com"}
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
