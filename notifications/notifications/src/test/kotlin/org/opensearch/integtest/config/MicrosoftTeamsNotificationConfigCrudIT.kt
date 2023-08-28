/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class MicrosoftTeamsNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete microsoft teams notification config using REST client`() {
        // Create sample config request reference
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.webhook.office.com/webhook2/test")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.MICROSOFT_TEAMS,
            isEnabled = true,
            configData = sampleMicrosoftTeams
        )

        // Create Microsoft Teams notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"microsoft_teams",
                "is_enabled":${referenceObject.isEnabled},
                "microsoft_teams":{"url":"${(referenceObject.configData as MicrosoftTeams).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get Microsoft Teams notification config

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
        val updatedMicrosoftTeams = MicrosoftTeams("https://updated.domain.webhook.office.com/webhook2/test")
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.MICROSOFT_TEAMS,
            isEnabled = true,
            configData = updatedMicrosoftTeams
        )

        // Update Microsoft Teams notification config
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"microsoft_teams",
                "is_enabled":${updatedObject.isEnabled},
                "microsoft_teams":{"url":"${(updatedObject.configData as MicrosoftTeams).url}"}
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

        // Get updated Microsoft Teams notification config

        val getUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, updatedObject, getUpdatedConfigResponse)
        Thread.sleep(100)

        // Delete Microsoft Teams notification config
        val deleteResponse = deleteConfig(configId)
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(configId).asString)
        Thread.sleep(1000)

        // Get Microsoft Teams notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Bad Request for multiple config data for microsoft teams using REST Client`() {
        // Create sample config request reference
        val sampleMicrosoftTeams = MicrosoftTeams("https://domain.webhook.office.com/1234567")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.MICROSOFT_TEAMS,
            isEnabled = true,
            configData = sampleMicrosoftTeams
        )

        // Create Microsoft Teams notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"microsoft_teams",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"https://dummy.com"}
                "microsoft_teams":{"url":"${(referenceObject.configData as MicrosoftTeams).url}"}
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
