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

import com.amazon.opendistroforelasticsearch.commons.notifications.model.ChannelMessage
import com.amazon.opendistroforelasticsearch.commons.notifications.model.Feature
import com.amazon.opendistroforelasticsearch.commons.notifications.model.NotificationInfo
import com.amazon.opendistroforelasticsearch.commons.notifications.model.SeverityType
import com.amazon.opendistroforelasticsearch.commons.utils.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.getJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SendNotificationRequestTests {

    private fun assertGetRequestEquals(
        expected: SendNotificationRequest,
        actual: SendNotificationRequest
    ) {
        assertEquals(expected.notificationInfo, actual.notificationInfo)
        assertEquals(expected.channelMessage, actual.channelMessage)
        assertEquals(expected.channelIds, actual.channelIds)
        assertEquals(expected.threadContext, actual.threadContext)
        assertNull(actual.validate())
    }

    @Test
    fun `Send request serialize and deserialize transport object should be equal`() {
        val notificationInfo = NotificationInfo(
            "title",
            "referenceId",
            Feature.Reports,
            SeverityType.High,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val recreatedObject = recreateObject(request) { SendNotificationRequest(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request serialize and deserialize using json object should be equal`() {
        val notificationInfo = NotificationInfo(
            "title",
            "referenceId",
            Feature.IndexManagement,
            SeverityType.Critical,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should safely ignore extra field in json object`() {
        val notificationInfo = NotificationInfo(
            "title",
            "referenceId",
            Feature.Alerting,
            SeverityType.High,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            "sample-thread-context"
        )
        val jsonString = """
        {
            "notificationInfo":{
                "title":"${notificationInfo.title}",
                "referenceId":"${notificationInfo.referenceId}",
                "feature":"${notificationInfo.feature}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channelMessage":{
                "textDescription":"${channelMessage.textDescription}",
                "htmlDescription":"${channelMessage.htmlDescription}"
            },
            "channelIds":["channelId1", "channelId2"],
            "context":"${request.threadContext}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should safely ignore thread context is absent in json object`() {
        val notificationInfo = NotificationInfo(
            "title",
            "referenceId",
            Feature.Reports,
            SeverityType.Info,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "textDescription",
            "<b>htmlDescription</b>",
            null
        )
        val request = SendNotificationRequest(
            notificationInfo,
            channelMessage,
            listOf("channelId1", "channelId2"),
            null
        )
        val jsonString = """
        {
            "notificationInfo":{
                "title":"${notificationInfo.title}",
                "referenceId":"${notificationInfo.referenceId}",
                "feature":"${notificationInfo.feature}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channelMessage":{
                "textDescription":"${channelMessage.textDescription}",
                "htmlDescription":"${channelMessage.htmlDescription}"
            },
            "channelIds":["channelId1", "channelId2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should throw exception if notificationInfo field is absent in json object`() {
        val jsonString = """
        {
            "channelMessage":{
                "textDescription":"textDescription"
            },
            "channelIds":["channelId1", "channelId2"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should throw exception if channelMessage field is absent in json object`() {
        val jsonString = """
        {
            "notificationInfo":{
                "title":"title",
                "referenceId":"referenceId",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channelIds":["channelId1", "channelId2"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request should throw exception if channelIds field is absent in json object`() {
        val jsonString = """
        {
            "notificationInfo":{
                "title":"title",
                "referenceId":"referenceId",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channelMessage":{
                "textDescription":"textDescription"
            }
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send request validate return exception if channelIds field is empty`() {
        val jsonString = """
        {
            "notificationInfo":{
                "title":"title",
                "referenceId":"referenceId",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channelMessage":{
                "textDescription":"textDescription"
            },
            "channelIds":[]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertNotNull(recreatedObject.validate())
    }
}
