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
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.PluginRestTestCase
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import java.util.EnumSet

class ChimeNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete chime notification config using REST client`() {
        // Create sample config request reference
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.Chime,
            EnumSet.of(Feature.Alerting, Feature.Reports),
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "notification_config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"Chime",
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${(referenceObject.configData!! as Chime).url}"}
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
        val updatedChime = Chime("https://updated.domain.com/updated_chime_url#0987654321")
        val updatedObject = NotificationConfig(
            "this is a updated config name",
            "this is a updated config description",
            ConfigType.Chime,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            configData = updatedChime
        )

        // Update chime notification config
        val updateRequestJsonString = """
        {
            "notification_config":{
                "name":"${updatedObject.name}",
                "description":"${updatedObject.description}",
                "config_type":"Chime",
                "features":[
                    "${updatedObject.features.elementAt(0)}"
                ],
                "is_enabled":${updatedObject.isEnabled},
                "chime":{"url":"${(updatedObject.configData!! as Chime).url}"}
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
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
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
}