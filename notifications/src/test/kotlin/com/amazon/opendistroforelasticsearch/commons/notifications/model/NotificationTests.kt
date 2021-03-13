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
import com.amazon.opendistroforelasticsearch.notifications.recreateObject
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NotificationTests : ESTestCase() {

    @Test
    fun `Notification serialize and deserialize should be equal`() {
        val sampleNotification = Notification(
            "title",
            "referenceId",
            Notification.SourceType.Alerting,
            severity = Notification.SeverityType.Info
        )
        val recreatedObject = recreateObject(sampleNotification) { Notification(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification serialize and deserialize using json should be equal`() {
        val sampleNotification = Notification(
            "title",
            "referenceId",
            Notification.SourceType.Alerting,
            severity = Notification.SeverityType.Info
        )

        val jsonString = getJsonString(sampleNotification)
        val recreatedObject = createObjectFromJsonString(jsonString) { Notification.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification should safely ignore extra field in json object`() {
        val sampleNotification = Notification(
            "title",
            "referenceId",
            Notification.SourceType.Alerting,
            tags = listOf("tag1", "tag2"),
            severity = Notification.SeverityType.Info
        )
        val jsonString = """
        { 
            "title":"title",
            "referenceId":"referenceId",
            "source":"Alerting",
            "severity":"Info",
            "tags":["tag1", "tag2"],
            "statusList":[],
            "extraField": "extra value"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Notification.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification should safely ignore unknown source type in json object`() {
        val sampleNotification = Notification(
            "title",
            "referenceId",
            Notification.SourceType.None,
            tags = listOf("tag1", "tag2"),
            severity = Notification.SeverityType.Info
        )
        val jsonString = """
        {
            "title":"title",
            "referenceId":"referenceId",
            "source": "NewSource",
            "severity":"Info",
            "tags":["tag1", "tag2"],
            "statusList":[]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Notification.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification throw exception if name is empty`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Notification(
                "",
                "referenceId",
                Notification.SourceType.Alerting,
                tags = listOf("tag1", "tag2"),
                severity = Notification.SeverityType.Info
            )
        }
    }
}
