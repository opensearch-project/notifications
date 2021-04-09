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
package com.amazon.opensearch.commons.notifications.action

import com.amazon.opensearch.commons.utils.createObjectFromJsonString
import com.amazon.opensearch.commons.utils.getJsonString
import com.amazon.opensearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DeleteNotificationConfigResponseTests {

    @Test
    fun `Delete response serialize and deserialize transport object should be equal`() {
        val configResponse = DeleteNotificationConfigResponse("sample_config_id")
        val recreatedObject = recreateObject(configResponse) { DeleteNotificationConfigResponse(it) }
        assertEquals(configResponse.configId, recreatedObject.configId)
    }

    @Test
    fun `Delete response serialize and deserialize using json object should be equal`() {
        val configResponse = DeleteNotificationConfigResponse("sample_config_id")
        val jsonString = getJsonString(configResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configResponse.configId, recreatedObject.configId)
    }

    @Test
    fun `Delete response should deserialize json object using parser`() {
        val configId = "sample_config_id"
        val jsonString = "{\"configId\":\"$configId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configId, recreatedObject.configId)
    }

    @Test
    fun `Delete response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should throw exception when configId is replace with configId2 in json object`() {
        val jsonString = "{\"configId2\":\"sample_config_id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should safely ignore extra field in json object`() {
        val configId = "sample_config_id"
        val jsonString = """
        {
            "configId":"$configId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteNotificationConfigResponse.parse(it) }
        assertEquals(configId, recreatedObject.configId)
    }
}
