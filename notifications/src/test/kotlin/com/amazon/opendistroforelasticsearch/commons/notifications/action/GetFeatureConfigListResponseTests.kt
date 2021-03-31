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

import com.amazon.opendistroforelasticsearch.commons.notifications.model.FeatureConfig
import com.amazon.opendistroforelasticsearch.commons.notifications.model.FeatureConfigList
import com.amazon.opendistroforelasticsearch.commons.notifications.model.NotificationConfig
import com.amazon.opendistroforelasticsearch.notifications.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.notifications.getJsonString
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import org.apache.lucene.search.TotalHits
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GetFeatureConfigListResponseTests : ESTestCase() {

    private fun assertSearchResultEquals(
        expected: FeatureConfigList,
        actual: FeatureConfigList
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Get Response serialize and deserialize with config object should be equal`() {
        val sampleConfig = FeatureConfig(
            "configId",
            "name",
            "description",
            NotificationConfig.ConfigType.Slack
        )
        val searchResult = FeatureConfigList(sampleConfig)
        val getResponse = GetFeatureConfigListResponse(searchResult)
        val recreatedObject = recreateObject(getResponse) { GetFeatureConfigListResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response serialize and deserialize with multiple config object should be equal`() {
        val sampleConfig1 = FeatureConfig(
            "configId1",
            "name1",
            "description1",
            NotificationConfig.ConfigType.Slack
        )
        val sampleConfig2 = FeatureConfig(
            "configId2",
            "name2",
            "description2",
            NotificationConfig.ConfigType.Chime
        )
        val sampleConfig3 = FeatureConfig(
            "configId3",
            "name3",
            "description3",
            NotificationConfig.ConfigType.Webhook
        )
        val searchResult = FeatureConfigList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val getResponse = GetFeatureConfigListResponse(searchResult)
        val recreatedObject = recreateObject(getResponse) { GetFeatureConfigListResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response serialize and deserialize using json config object should be equal`() {
        val sampleConfig = FeatureConfig(
            "configId",
            "name",
            "description",
            NotificationConfig.ConfigType.EmailGroup
        )
        val searchResult = FeatureConfigList(sampleConfig)
        val getResponse = GetFeatureConfigListResponse(searchResult)
        val jsonString = getJsonString(getResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response serialize and deserialize using json with multiple config object should be equal`() {
        val sampleConfig1 = FeatureConfig(
            "configId1",
            "name1",
            "description1",
            NotificationConfig.ConfigType.Slack
        )
        val sampleConfig2 = FeatureConfig(
            "configId2",
            "name2",
            "description2",
            NotificationConfig.ConfigType.Chime
        )
        val sampleConfig3 = FeatureConfig(
            "configId3",
            "name3",
            "description3",
            NotificationConfig.ConfigType.Webhook
        )
        val searchResult = FeatureConfigList(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(sampleConfig1, sampleConfig2, sampleConfig3)
        )
        val getResponse = GetFeatureConfigListResponse(searchResult)
        val jsonString = getJsonString(getResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response should use isEnabled=true if absent in json object`() {
        val sampleConfig = FeatureConfig(
            "configId",
            "name",
            "description",
            NotificationConfig.ConfigType.Email,
            true
        )
        val searchResult = FeatureConfigList(sampleConfig)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "featureConfigList":[
                {
                    "configId":"configId",
                    "name":"name",
                    "description":"description",
                    "configType":"Email"
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response should safely ignore extra field in json object`() {
        val sampleConfig = FeatureConfig(
            "configId",
            "name",
            "description",
            NotificationConfig.ConfigType.Email
        )
        val searchResult = FeatureConfigList(sampleConfig)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "featureConfigList":[
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
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val sampleConfig = FeatureConfig(
            "configId",
            "name",
            "description",
            NotificationConfig.ConfigType.Email
        )
        val searchResult = FeatureConfigList(sampleConfig)
        val jsonString = """
        {
            "featureConfigList":[
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
        val recreatedObject = createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Get Response should throw exception if featureConfigList is absent in json`() {
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetFeatureConfigListResponse.parse(it) }
        }
    }
}
