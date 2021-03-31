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
package com.amazon.opendistroforelasticsearch.commons.notifications.action

import com.amazon.opendistroforelasticsearch.commons.notifications.model.NotificationConfig
import com.amazon.opendistroforelasticsearch.notifications.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.notifications.getJsonString
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetFeatureConfigListRequestTests : ESTestCase() {

    private fun assertGetRequestEquals(
        expected: GetFeatureConfigListRequest,
        actual: GetFeatureConfigListRequest
    ) {
        assertEquals(expected.feature, actual.feature)
        assertEquals(expected.threadContext, actual.threadContext)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val configRequest = GetFeatureConfigListRequest(NotificationConfig.Feature.Reports, "sample-thread-context")
        val recreatedObject = recreateObject(configRequest) { GetFeatureConfigListRequest(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val configRequest =
            GetFeatureConfigListRequest(NotificationConfig.Feature.IndexManagement, "sample-thread-context")
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetFeatureConfigListRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val configRequest = GetFeatureConfigListRequest(NotificationConfig.Feature.Alerting, "sample-thread-context")
        val jsonString = """
        {
            "feature":"${configRequest.feature}",
            "context":"${configRequest.threadContext}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should safely ignore thread context is absent in json object`() {
        val configRequest = GetFeatureConfigListRequest(NotificationConfig.Feature.Reports, null)
        val jsonString = """
        {
            "feature":"${configRequest.feature}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should throw exception if feature field is absent in json object`() {
        val jsonString = """
        {
            "context":"sample-thread-context"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { GetFeatureConfigListRequest.parse(it) }
        }
    }
}
