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

package com.amazon.opendistroforelasticsearch.notifications.resthandler

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import com.amazon.opendistroforelasticsearch.notifications.ODFERestTestCase
import org.elasticsearch.client.Request
import org.elasticsearch.client.RequestOptions
import org.junit.Assert

class NotificationConfigCrudIT : ODFERestTestCase() {

    fun `test Create notification config using REST client`() {
        val request = Request("POST", "$PLUGIN_BASE_URI/configs")
        val jsonString = """
        {
            "notificationConfig":{
                "name":"name",
                "description":"description",
                "configType":"Slack",
                "features":["IndexManagement"],
                "isEnabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        request.setJsonEntity(jsonString)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        val retVal = executeRequest(request)
        Assert.assertNotNull(retVal.get("configId").asString)
    }
}
