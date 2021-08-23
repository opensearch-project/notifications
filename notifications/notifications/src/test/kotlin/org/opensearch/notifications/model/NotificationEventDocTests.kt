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
package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_ALERTING
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.notifications.createObjectFromJsonString
import org.opensearch.notifications.getJsonString
import java.time.Instant

internal class NotificationEventDocTests {

    @Test
    fun `Event doc serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val status = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(status))
        val eventDoc = NotificationEventDoc(metadata, sampleEvent)
        val jsonString = getJsonString(eventDoc)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventDoc.parse(it) }
        assertEquals(eventDoc, recreatedObject)
    }

    @Test
    fun `Event doc should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val eventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val eventStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(eventSource, listOf(eventStatus))
        val eventDoc = NotificationEventDoc(metadata, sampleEvent)
        val jsonString = """
        {
            "metadata":{
                "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                "tenant":"selectedTenant",
                "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
            },
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":["tag1", "tag2"]
                },
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
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventDoc.parse(it) }
        assertEquals(eventDoc, recreatedObject)
    }

    @Test
    fun `Event doc should throw exception if metadata is absent in json`() {
        val jsonString = """
        {
            "event":{
                "event_source":{
                    "title":"title",
                    "reference_id":"reference_id",
                    "feature":"alerting",
                    "severity":"info",
                    "tags":["tag1", "tag2"]
                },
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
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventDoc.parse(it) }
        }
    }

    @Test
    fun `Event doc should throw exception if event is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "metadata":{
                "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                "tenant":"selectedTenant",
                "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventDoc.parse(it) }
        }
    }
}
