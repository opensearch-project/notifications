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

import com.amazon.opendistroforelasticsearch.commons.utils.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.getJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ChannelStatusTests {

    @Test
    fun `Notification Status serialize and deserialize should be equal`() {
        val sampleStatus = ChannelStatus(
            "configId",
            "name",
            ConfigType.Slack,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val recreatedObject = recreateObject(sampleStatus) { ChannelStatus(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status serialize and deserialize using json should be equal`() {
        val sampleStatus = ChannelStatus(
            "configId",
            "name",
            ConfigType.Slack,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )

        val jsonString = getJsonString(sampleStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelStatus.parse(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status should safely ignore extra field in json object`() {
        val sampleStatus = ChannelStatus(
            "configId",
            "name",
            ConfigType.Slack,
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
           "extra_field_1":["extra", "value"],
           "extra_field_2":{"extra":"value"},
           "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { ChannelStatus.parse(it) }
        assertEquals(sampleStatus, recreatedObject)
    }

    @Test
    fun `Notification Status should throw exception when config type is email with empty emailRecipientList`() {
        val jsonString = """
        {
           "configId":"configId",
           "configType":"Email",
           "configName":"name",
           "deliveryStatus":
           {
                "statusCode":"404",
                "statusText":"invalid recipient"
           },
           "emailRecipientStatus":[]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { ChannelStatus.parse(it) }
        }
    }

    @Test
    fun `Notification should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { NotificationInfo.parse(it) }
        }
    }

    @Test
    fun `Notification throw exception if deliveryStatus is empty for config type Slack`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChannelStatus(
                "configId",
                "name",
                ConfigType.Slack
            )
        }
    }

    @Test
    fun `Notification throw exception if deliveryStatus is empty for config type Chime`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChannelStatus(
                "configId",
                "name",
                ConfigType.Chime
            )
        }
    }

    @Test
    fun `Notification throw exception if deliveryStatus is empty for config type Webhook`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChannelStatus(
                "configId",
                "name",
                ConfigType.Webhook
            )
        }
    }

    @Test
    fun `Notification throw exception if emailRecipientStatus is empty for config type Email`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChannelStatus(
                "configId",
                "name",
                ConfigType.Email
            )
        }
    }
}
