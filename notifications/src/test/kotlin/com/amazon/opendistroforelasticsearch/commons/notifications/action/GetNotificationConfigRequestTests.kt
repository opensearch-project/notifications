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

import com.amazon.opendistroforelasticsearch.commons.utils.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.getJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GetNotificationConfigRequestTests {

    private fun assertGetRequestEquals(
        expected: GetNotificationConfigRequest,
        actual: GetNotificationConfigRequest
    ) {
        assertEquals(expected.fromIndex, actual.fromIndex)
        assertEquals(expected.maxItems, actual.maxItems)
        assertEquals(expected.configId, actual.configId)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val configRequest = GetNotificationConfigRequest(0, 10, "sample_config_id")
        val recreatedObject = recreateObject(configRequest) { GetNotificationConfigRequest(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val configRequest = GetNotificationConfigRequest(0, 10, "sample_config_id")
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with all field should deserialize json object using parser`() {
        val configRequest = GetNotificationConfigRequest(10, 100, "sample_config_id")
        val jsonString = """
        {
            "fromIndex":"10",
            "maxItems":"100",
            "configId":"${configRequest.configId}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request without configId field should deserialize json object using parser`() {
        val configRequest = GetNotificationConfigRequest(20, 200)
        val jsonString = """
        {
            "fromIndex":"20",
            "maxItems":"200"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request without fromIndex field should deserialize json object using parser`() {
        val configRequest = GetNotificationConfigRequest(maxItems = 100, configId = "sample_config_id")
        val jsonString = """
        {
            "maxItems":"100",
            "configId":"${configRequest.configId}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request without maxItems field should deserialize json object using parser`() {
        val configRequest = GetNotificationConfigRequest(fromIndex = 10, configId = "sample_config_id")
        val jsonString = """
        {
            "fromIndex":"10",
            "configId":"${configRequest.configId}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request no field should deserialize json object using parser`() {
        val configRequest = GetNotificationConfigRequest()
        val jsonString = """
        {
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val configRequest = GetNotificationConfigRequest(configId = "sample_config_id")
        val jsonString = """
        {
            "configId":"${configRequest.configId}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationConfigRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }
}
