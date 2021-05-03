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

internal class FilterConfigListTests {

    private fun assertSearchResultEquals(
        expected: FeatureChannelList,
        actual: FeatureChannelList
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.Slack
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val recreatedObject = recreateObject(searchResult) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val sampleConfig1 = FeatureChannel(
            "config_id1",
            "name1",
            "description1",
            ConfigType.Slack
        )
        val sampleConfig2 = FeatureChannel(
            "config_id2",
            "name2",
            "description2",
            ConfigType.Chime
        )
        val sampleConfig3 = FeatureChannel(
            "config_id3",
            "name3",
            "description3",
            ConfigType.Webhook
        )
        val searchResult = FeatureChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val recreatedObject = recreateObject(searchResult) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json config object should be equal`() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.EmailGroup
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple config object should be equal`() {
        val sampleConfig1 = FeatureChannel(
            "config_id1",
            "name1",
            "description1",
            ConfigType.Slack
        )
        val sampleConfig2 = FeatureChannel(
            "config_id2",
            "name2",
            "description2",
            ConfigType.Chime
        )
        val sampleConfig3 = FeatureChannel(
            "config_id3",
            "name3",
            "description3",
            ConfigType.Webhook
        )
        val searchResult = FeatureChannelList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should use isEnabled=true if absent in json object`() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.Email,
            true
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "feature_channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"Email"
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.Email
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq",
            "feature_channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"Email",
                    "is_enabled":true
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val sampleConfig = FeatureChannel(
            "config_id",
            "name",
            "description",
            ConfigType.Email
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "feature_channel_list":[
                {
                    "config_id":"config_id",
                    "name":"name",
                    "description":"description",
                    "config_type":"Email",
                    "is_enabled":true
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should throw exception if featureChannelList is absent in json`() {
        val jsonString = """
        {
            "start_index":"0",
            "total_hits":"1",
            "total_hit_relation":"eq"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        }
    }
}
