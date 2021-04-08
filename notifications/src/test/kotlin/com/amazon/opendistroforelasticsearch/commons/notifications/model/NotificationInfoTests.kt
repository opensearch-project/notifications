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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NotificationInfoTests {

    @Test
    fun `Notification serialize and deserialize should be equal`() {
        val sampleNotification = NotificationInfo(
            "title",
            "referenceId",
            Feature.Alerting,
            severity = SeverityType.Info
        )
        val recreatedObject = recreateObject(sampleNotification) { NotificationInfo(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification serialize and deserialize using json should be equal`() {
        val sampleNotification = NotificationInfo(
            "title",
            "referenceId",
            Feature.Alerting,
            severity = SeverityType.Info
        )

        val jsonString = getJsonString(sampleNotification)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationInfo.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification should safely ignore extra field in json object`() {
        val sampleNotification = NotificationInfo(
            "title",
            "referenceId",
            Feature.Alerting,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.Info
        )
        val jsonString = """
        { 
            "title":"title",
            "referenceId":"referenceId",
            "feature":"Alerting",
            "severity":"Info",
            "tags":["tag1", "tag2"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationInfo.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification should safely ignore unknown feature type in json object`() {
        val sampleNotification = NotificationInfo(
            "title",
            "referenceId",
            Feature.None,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.Info
        )
        val jsonString = """
        {
            "title":"title",
            "referenceId":"referenceId",
            "feature": "NewFeature",
            "severity":"Info",
            "tags":["tag1", "tag2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationInfo.parse(it) }
        assertEquals(sampleNotification, recreatedObject)
    }

    @Test
    fun `Notification throw exception if name is empty`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationInfo(
                "",
                "referenceId",
                Feature.Alerting,
                tags = listOf("tag1", "tag2"),
                severity = SeverityType.Info
            )
        }
    }
}
