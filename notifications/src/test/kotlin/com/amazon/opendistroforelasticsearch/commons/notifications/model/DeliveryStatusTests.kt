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

package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.notifications.getJsonString
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DeliveryStatusTests : ESTestCase() {
    @Test
    fun `DeliveryStatus serialize and deserialize should be equal`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val recreatedObject = recreateObject(sampleDeliveryStatus) { DeliveryStatus(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }

    @Test
    fun `DeliveryStatus serialize and deserialize using json should be equal`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val jsonString = getJsonString(sampleDeliveryStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }

    @Test
    fun `DeliveryStatus should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        }
    }

    @Test
    fun `DeliveryStatus should safely ignore extra field in json object`() {
        val sampleDeliveryStatus = DeliveryStatus(
            "404",
            "invalid recipient"
        )
        val jsonString = """
        {
            "statusCode": "404",
            "statusText": "invalid recipient",
            "extra": "field"
        }    
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeliveryStatus.parse(it) }
        assertEquals(sampleDeliveryStatus, recreatedObject)
    }
}
