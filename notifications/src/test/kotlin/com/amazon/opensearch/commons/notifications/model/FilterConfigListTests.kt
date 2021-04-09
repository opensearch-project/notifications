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
package com.amazon.opensearch.commons.notifications.model

import com.amazon.opensearch.commons.utils.createObjectFromJsonString
import com.amazon.opensearch.commons.utils.getJsonString
import com.amazon.opensearch.commons.utils.recreateObject
import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
            "configId",
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
            "configId1",
            "name1",
            "description1",
            ConfigType.Slack
        )
        val sampleConfig2 = FeatureChannel(
            "configId2",
            "name2",
            "description2",
            ConfigType.Chime
        )
        val sampleConfig3 = FeatureChannel(
            "configId3",
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
            "configId",
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
            "configId1",
            "name1",
            "description1",
            ConfigType.Slack
        )
        val sampleConfig2 = FeatureChannel(
            "configId2",
            "name2",
            "description2",
            ConfigType.Chime
        )
        val sampleConfig3 = FeatureChannel(
            "configId3",
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
            "configId",
            "name",
            "description",
            ConfigType.Email,
            true
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "featureChannelList":[
                {
                    "configId":"configId",
                    "name":"name",
                    "description":"description",
                    "configType":"Email"
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
            "configId",
            "name",
            "description",
            ConfigType.Email
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "featureChannelList":[
                {
                    "configId":"configId",
                    "name":"name",
                    "description":"description",
                    "configType":"Email",
                    "isEnabled":true
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
            "configId",
            "name",
            "description",
            ConfigType.Email
        )
        val searchResult = FeatureChannelList(sampleConfig)
        val jsonString = """
        {
            "featureChannelList":[
                {
                    "configId":"configId",
                    "name":"name",
                    "description":"description",
                    "configType":"Email",
                    "isEnabled":true
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
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { FeatureChannelList(it) }
        }
    }
}
