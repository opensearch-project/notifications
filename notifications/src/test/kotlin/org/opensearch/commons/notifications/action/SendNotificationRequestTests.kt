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
package org.opensearch.commons.notifications.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.NotificationInfo
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

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
            "reference_id",
            Feature.Reports,
            SeverityType.High,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
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
            "reference_id",
            Feature.IndexManagement,
            SeverityType.Critical,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
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
            "reference_id",
            Feature.Alerting,
            SeverityType.High,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
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
            "notification_info":{
                "title":"${notificationInfo.title}",
                "reference_id":"${notificationInfo.referenceId}",
                "feature":"${notificationInfo.feature}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"${channelMessage.textDescription}",
                "html_description":"${channelMessage.htmlDescription}"
            },
            "channel_ids":["channelId1", "channelId2"],
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
            "reference_id",
            Feature.Reports,
            SeverityType.Info,
            listOf("tag1", "tag2")
        )
        val channelMessage = ChannelMessage(
            "text_description",
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
            "notification_info":{
                "title":"${notificationInfo.title}",
                "reference_id":"${notificationInfo.referenceId}",
                "feature":"${notificationInfo.feature}",
                "severity":"${notificationInfo.severity}",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"${channelMessage.textDescription}",
                "html_description":"${channelMessage.htmlDescription}"
            },
            "channel_ids":["channelId1", "channelId2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertGetRequestEquals(request, recreatedObject)
    }

    @Test
    fun `Send request should throw exception if notificationInfo field is absent in json object`() {
        val jsonString = """
        {
            "channel_message":{
                "text_description":"text_description"
            },
            "channel_ids":["channelId1", "channelId2"]
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
            "notification_info":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_ids":["channelId1", "channelId2"]
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
            "notification_info":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"text_description"
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
            "notification_info":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"feature",
                "severity":"High",
                "tags":["tag1", "tag2"]
            },
            "channel_message":{
                "text_description":"text_description"
            },
            "channel_ids":[]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendNotificationRequest.parse(it) }
        assertNotNull(recreatedObject.validate())
    }
}
