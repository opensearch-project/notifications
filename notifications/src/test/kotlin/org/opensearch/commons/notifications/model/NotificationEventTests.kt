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
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class NotificationEventTests {

    @Test
    fun `Notification event serialize and deserialize should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val recreatedObject = recreateObject(sampleEvent) { NotificationEvent(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event serialize and deserialize using json should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.REPORTS,
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val jsonString = getJsonString(sampleEvent)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event should safely ignore extra field in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val status1 = EventStatus(
            "config_id1",
            "name 1",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val status2 = EventStatus(
            "config_id2",
            "name 2",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("503", "service unavailable")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(status1, status2))
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            },
            "status_list":[
                {
                   "config_id":"config_id1",
                   "config_type":"chime",
                   "config_name":"name 1",
                   "delivery_status":
                   {
                        "status_code":"200",
                        "status_text":"success"
                   }
                },
                {
                   "config_id":"config_id2",
                   "config_type":"slack",
                   "config_name":"name 2",
                   "delivery_status":
                   {
                        "status_code":"503",
                        "status_text":"service unavailable"
                   }
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        assertEquals(sampleEvent, recreatedObject)
    }

    @Test
    fun `Notification event throw exception if event source is absent`() {
        val jsonString = """
        {
            "status_list":[
                {
                   "config_id":"config_id",
                   "config_type":"chime",
                   "config_name":"name",
                   "delivery_status":
                   {
                        "status_code":"200",
                        "status_text":"success"
                   }
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }

    @Test
    fun `Notification event throw exception if status_list is absent`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }

    @Test
    fun `Notification event throw exception if status_list is empty`() {
        val jsonString = """
        {
            "event_source":{
                "title":"title",
                "reference_id":"reference_id",
                "feature":"alerting",
                "severity":"info",
                "tags":["tag1", "tag2"]
            },
            "status_list":[]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEvent.parse(it) }
        }
    }
}
