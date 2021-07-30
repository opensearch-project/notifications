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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.integtest.send

import org.junit.Assert
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

internal class SendTestMessageRestHandlerIT : PluginRestTestCase() {
    @Suppress("EmptyFunctionBlock")
    fun `test send test chime message`() {
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"chime",
                "feature_list":[
                    "index_management",
                    "reports",
                    "alerting"
                ],
                "is_enabled":true,
                "chime":{
                    "url":"https://hooks.chime.aws/incomingwebhooks/xxxx"
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

        // send test message
        val sendResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/feature/test/$configId?feature=alerting",
            "",
            RestStatus.OK.status
        )
        val eventId = sendResponse.get("event_id").asString

        val getEventResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/events/$eventId",
            "",
            RestStatus.OK.status
        )
        val items = getEventResponse.get("event_list").asJsonArray
        Assert.assertEquals(1, items.size())
        val getResponseItem = items[0].asJsonObject
        Assert.assertEquals(eventId, getResponseItem.get("event_id").asString)
        Assert.assertEquals("", getResponseItem.get("tenant").asString)
        Assert.assertNotNull(getResponseItem.get("event").asJsonObject)
        Thread.sleep(100)
    }

    @Suppress("EmptyFunctionBlock")
    fun `test send test slack message`() {
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"slack",
                "feature_list":[
                    "index_management",
                    "reports",
                    "alerting"
                ],
                "is_enabled":true,
                "slack":{
                    "url":"https://hooks.slack.com/services/xxx/xxx"
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

        // send test message
        val sendResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/feature/test/$configId?feature=alerting",
            "",
            RestStatus.OK.status
        )
        val eventId = sendResponse.get("event_id").asString

        val getEventResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/events/$eventId",
            "",
            RestStatus.OK.status
        )
        val items = getEventResponse.get("event_list").asJsonArray
        Assert.assertEquals(1, items.size())
        val getResponseItem = items[0].asJsonObject
        Assert.assertEquals(eventId, getResponseItem.get("event_id").asString)
        Assert.assertEquals("", getResponseItem.get("tenant").asString)
        Assert.assertNotNull(getResponseItem.get("event").asJsonObject)
        Thread.sleep(100)
    }

    @Suppress("EmptyFunctionBlock")
    fun `test send custom webhook message`() {
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"webhook",
                "feature_list":[
                    "index_management",
                    "reports",
                    "alerting"
                ],
                "is_enabled":true,
                "webhook":{
                    "url":"https://xxx.com/my-webhook@dev",
                    "header_params": {
                       "Content-type": "text/plain"
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

        // send test message
        val sendResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/feature/test/$configId?feature=alerting",
            "",
            RestStatus.OK.status
        )
        val eventId = sendResponse.get("event_id").asString

        val getEventResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/events/$eventId",
            "",
            RestStatus.OK.status
        )
        val items = getEventResponse.get("event_list").asJsonArray
        Assert.assertEquals(1, items.size())
        val getResponseItem = items[0].asJsonObject
        Assert.assertEquals(eventId, getResponseItem.get("event_id").asString)
        Assert.assertEquals("", getResponseItem.get("tenant").asString)
        Assert.assertNotNull(getResponseItem.get("event").asJsonObject)
        Thread.sleep(100)
    }
}
