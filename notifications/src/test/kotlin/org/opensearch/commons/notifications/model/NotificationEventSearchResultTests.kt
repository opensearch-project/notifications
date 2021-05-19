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

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import java.time.Instant

internal class NotificationEventSearchResultTests {

    private fun assertSearchResultEquals(
        expected: NotificationEventSearchResult,
        actual: NotificationEventSearchResult
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with event object should be equal`() {
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
        val eventInfo = NotificationEventInfo(
            "event_id",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleEvent
        )
        val searchResult = NotificationEventSearchResult(eventInfo)
        val recreatedObject = recreateObject(searchResult) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize with multiple event status object should be equal`() {
        val eventSource1 = EventSource(
            "title 1",
            "reference_id_1",
            Feature.ALERTING,
            severity = SeverityType.INFO
        )
        val eventSource2 = EventSource(
            "title 2",
            "reference_id_2",
            Feature.REPORTS,
            severity = SeverityType.HIGH
        )
        val status1 = EventStatus(
            "config_id1",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val status2 = EventStatus(
            "config_id2",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val eventInfo1 = NotificationEventInfo(
            "event_id1",
            Instant.now(),
            Instant.now(),
            "tenant",
            NotificationEvent(eventSource1, listOf(status1))
        )
        val eventInfo2 = NotificationEventInfo(
            "event_id2",
            Instant.now(),
            Instant.now(),
            "tenant",
            NotificationEvent(eventSource2, listOf(status2))
        )
        val eventInfo3 = NotificationEventInfo(
            "event_id3",
            Instant.now(),
            Instant.now(),
            "tenant",
            NotificationEvent(eventSource1, listOf(status1, status2))
        )
        val eventInfo4 = NotificationEventInfo(
            "event_id4",
            Instant.now(),
            Instant.now(),
            "tenant",
            NotificationEvent(eventSource2, listOf(status1, status2))
        )
        val searchResult = NotificationEventSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(eventInfo1, eventInfo2, eventInfo3, eventInfo4)
        )
        val recreatedObject = recreateObject(searchResult) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json event object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
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
        val eventInfo = NotificationEventInfo(
            "event_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            sampleEvent
        )
        val searchResult = NotificationEventSearchResult(eventInfo)
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple event object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val eventSource1 = EventSource(
            "title 1",
            "reference_id_1",
            Feature.ALERTING,
            severity = SeverityType.INFO
        )
        val eventSource2 = EventSource(
            "title 2",
            "reference_id_2",
            Feature.REPORTS,
            severity = SeverityType.HIGH
        )
        val status1 = EventStatus(
            "config_id1",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val status2 = EventStatus(
            "config_id2",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )
        val eventInfo1 = NotificationEventInfo(
            "event_id1",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            NotificationEvent(eventSource1, listOf(status1))
        )
        val eventInfo2 = NotificationEventInfo(
            "event_id2",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            NotificationEvent(eventSource2, listOf(status2))
        )
        val searchResult = NotificationEventSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(eventInfo1, eventInfo2)
        )
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
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
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val eventInfo = NotificationEventInfo(
            "event_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            sampleEvent
        )
        val searchResult = NotificationEventSearchResult(eventInfo)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "event_list":[
                {
                    "event_id":"event_id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant",
                    "event":{
                        "event_source":{
                            "title":"title",
                            "reference_id":"reference_id",
                            "feature":"alerting",
                            "severity":"info",
                            "tags":[]
                        },
                        "status_list":[
                            {
                               "config_id":"config_id",
                               "config_type":"slack",
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
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
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
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))
        val eventInfo = NotificationEventInfo(
            "event_id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            sampleEvent
        )
        val searchResult = NotificationEventSearchResult(eventInfo)
        val jsonString = """
        {
            "event_list":[
                {
                    "event_id":"event_id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant",
                    "event":{
                        "event_source":{
                            "title":"title",
                            "reference_id":"reference_id",
                            "feature":"alerting",
                            "severity":"info",
                            "tags":[]
                        },
                        "status_list":[
                            {
                               "config_id":"config_id",
                               "config_type":"slack",
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
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationEventSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should throw exception if event is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "event_list":[
                {
                    "event_id":"event_id",
                    "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant"
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationEventSearchResult(it) }
        }
    }
}
