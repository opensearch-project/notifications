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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import org.opensearch.search.sort.SortOrder

internal class GetNotificationEventRequestTests {

    private fun assertGetRequestEquals(
        expected: GetNotificationEventRequest,
        actual: GetNotificationEventRequest
    ) {
        assertEquals(expected.eventIds, actual.eventIds)
        assertEquals(expected.fromIndex, actual.fromIndex)
        assertEquals(expected.maxItems, actual.maxItems)
        assertEquals(expected.sortField, actual.sortField)
        assertEquals(expected.sortOrder, actual.sortOrder)
        assertEquals(expected.filterParams, actual.filterParams)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val configRequest = GetNotificationEventRequest(
            setOf("sample_event_id"),
            0,
            10,
            "sortField",
            SortOrder.DESC,
            mapOf(Pair("filterKey", "filterValue"))
        )
        val recreatedObject = recreateObject(configRequest) { GetNotificationEventRequest(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val configRequest = GetNotificationEventRequest(
            setOf("sample_event_id"),
            0,
            10,
            "sortField",
            SortOrder.ASC,
            mapOf(Pair("filterKey", "filterValue"))
        )
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with all field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(
            setOf("sample_event_id"),
            10,
            100,
            "sortField",
            SortOrder.DESC,
            mapOf(
                Pair("filterKey1", "filterValue1"),
                Pair("filterKey2", "true"),
                Pair("filterKey3", "filter,Value,3"),
                Pair("filterKey4", "4")
            )
        )
        val jsonString = """
        {
            "event_id_list":["${configRequest.eventIds.first()}"],
            "from_index":"10",
            "max_items":"100",
            "sort_field":"sortField",
            "sort_order":"desc",
            "filter_param_list": {
                "filterKey1":"filterValue1",
                "filterKey2":"true",
                "filterKey3":"filter,Value,3",
                "filterKey4":"4"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only event_id field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(eventIds = setOf("sample_event_id"))
        val jsonString = """
        {
            "event_id_list":["${configRequest.eventIds.first()}"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only from_index field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(fromIndex = 20)
        val jsonString = """
        {
            "from_index":"20"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only max_items field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(maxItems = 100)
        val jsonString = """
        {
            "max_items":"100"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only sort_field field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(sortField = "sample_sort_field")
        val jsonString = """
        {
            "sort_field":"sample_sort_field"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only sort_order=asc field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(sortOrder = SortOrder.ASC)
        val jsonString = """
        {
            "sort_order":"asc"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only sort_order=ASC field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(sortOrder = SortOrder.ASC)
        val jsonString = """
        {
            "sort_order":"ASC"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only sort_order=desc field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(sortOrder = SortOrder.DESC)
        val jsonString = """
        {
            "sort_order":"desc"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with only sort_order=DESC field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(sortOrder = SortOrder.DESC)
        val jsonString = """
        {
            "sort_order":"DESC"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request with invalid sort_order should throw exception`() {
        val jsonString = """
        {
            "sort_order":"descending"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        }
    }

    @Test
    fun `Get request with only filter_param_list field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest(
            filterParams = mapOf(
                Pair("filterKey1", "filterValue1"),
                Pair("filterKey2", "true"),
                Pair("filterKey3", "filter,Value,3"),
                Pair("filterKey4", "4")
            )
        )
        val jsonString = """
        {
            "filter_param_list": {
                "filterKey1":"filterValue1",
                "filterKey2":"true",
                "filterKey3":"filter,Value,3",
                "filterKey4":"4"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request no field should deserialize json object using parser`() {
        val configRequest = GetNotificationEventRequest()
        val jsonString = """
        {
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val configRequest = GetNotificationEventRequest(eventIds = setOf("sample_event_id"))
        val jsonString = """
        {
            "event_id_list":["${configRequest.eventIds.first()}"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetNotificationEventRequest.parse(it) }
        assertGetRequestEquals(configRequest, recreatedObject)
    }
}
