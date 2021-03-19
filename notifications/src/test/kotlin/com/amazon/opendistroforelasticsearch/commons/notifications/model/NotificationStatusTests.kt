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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NotificationStatusTests : ESTestCase() {

    @Test
    fun `Notification Status serialize and deserialize should be equal`() {
        val sampleStatus = NotificationStatus(
            "configId",
            "name",
            NotificationConfig.ConfigType.Slack,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val recreatedObject = recreateObject(sampleStatus) { NotificationStatus(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status serialize and deserialize using json should be equal`() {
        val sampleStatus = NotificationStatus(
            "configId",
            "name",
            NotificationConfig.ConfigType.Slack,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )

        val jsonString = getJsonString(sampleStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationStatus.parse(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status should safely ignore extra field in json object`() {
        val sampleStatus = NotificationStatus(
            "configId",
            "name",
            NotificationConfig.ConfigType.Slack,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val jsonString = """
        {
           "configId":"configId",
           "configType":"Slack",
           "configName":"name",
           "emailRecipientStatus":[],
           "deliveryStatus":
           {
                "statusCode":"404",
                "statusText":"invalid recipient"
           },
           "extraField": "extra field"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationStatus.parse(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status should throw exception when config type is email with empty emailRecipientList`() {
        val jsonString = """
        {
           "configId":"configId",
           "configType":"Slack",
           "configName":"name",
           "emailRecipientStatus":[]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { NotificationStatus.parse(it) }
        }
    }

    @Test
    fun `Notification should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Notification.parse(it) }
        }
    }

    @Test
    fun `Notification throw exception if deliveryStatus is empty for config type Slack`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationStatus(
                "configId",
                "name",
                NotificationConfig.ConfigType.Slack
            )
        }
    }
}
