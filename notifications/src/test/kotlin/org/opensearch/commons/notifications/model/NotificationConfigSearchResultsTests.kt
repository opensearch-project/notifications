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
import java.util.EnumSet

internal class NotificationConfigSearchResultsTests {

    private fun assertSearchResultEquals(
        expected: NotificationConfigSearchResult,
        actual: NotificationConfigSearchResult
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "configId",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val recreatedObject = recreateObject(searchResult) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val sampleConfig1 = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            configData = Slack("https://domain.com/sample_url#1234567890")
        )
        val configInfo1 = NotificationConfigInfo(
            "configId1",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleConfig1
        )
        val sampleConfig2 = NotificationConfig(
            "name",
            "description",
            ConfigType.Chime,
            EnumSet.of(Feature.IndexManagement),
            configData = Chime("https://domain.com/sample_url#1234567890")
        )
        val configInfo2 = NotificationConfigInfo(
            "configId2",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleConfig2
        )
        val searchResult = NotificationConfigSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(configInfo1, configInfo2)
        )
        val recreatedObject = recreateObject(searchResult) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "configId",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleConfig1 = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            configData = Slack("https://domain.com/sample_url#1234567890")
        )
        val configInfo1 = NotificationConfigInfo(
            "configId1",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            sampleConfig1
        )
        val sampleConfig2 = NotificationConfig(
            "name",
            "description",
            ConfigType.Chime,
            EnumSet.of(Feature.IndexManagement),
            configData = Chime("https://domain.com/sample_url#1234567890")
        )
        val configInfo2 = NotificationConfigInfo(
            "configId2",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            sampleConfig2
        )
        val searchResult = NotificationConfigSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(configInfo1, configInfo2)
        )
        val jsonString = getJsonString(searchResult)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "notificationConfigList":[
                {
                    "configId":"config-Id",
                    "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "createdTimeMs":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant",
                    "notificationConfig":{
                        "name":"name",
                        "description":"description",
                        "configType":"Slack",
                        "features":["IndexManagement"],
                        "isEnabled":true,
                        "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
                    }
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            sampleConfig
        )
        val searchResult = NotificationConfigSearchResult(configInfo)
        val jsonString = """
        {
            "notificationConfigList":[
                {
                    "configId":"config-Id",
                    "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "createdTimeMs":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant",
                    "notificationConfig":{
                        "name":"name",
                        "description":"description",
                        "configType":"Slack",
                        "features":["IndexManagement"],
                        "isEnabled":true,
                        "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
                    }
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigSearchResult(it) }
        assertSearchResultEquals(searchResult, recreatedObject)
    }

    @Test
    fun `Search result should throw exception if notificationConfigs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHits":"1",
            "totalHitRelation":"eq",
            "notificationConfigList":[
                {
                    "configId":"config-Id",
                    "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "createdTimeMs":"${createdTimeMs.toEpochMilli()}",
                    "tenant":"selectedTenant"
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigSearchResult(it) }
        }
    }
}
