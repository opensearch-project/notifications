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
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.EnumSet

internal class NotificationConfigInfoTests : ESTestCase() {

    @Test
    fun `Config info serialize and deserialize with config object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            slack = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "configId",
            Instant.now(),
            Instant.now(),
            "tenant",
            sampleConfig
        )
        val recreatedObject = recreateObject(configInfo) { NotificationConfigInfo(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            slack = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "configId",
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            sampleConfig
        )
        val jsonString = getJsonString(configInfo)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info should take default tenant when field is absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "", // Default tenant
            sampleConfig
        )
        val jsonString = """
        {
            "configId":"config-Id",
            "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
            "createdTimeMs":"${createdTimeMs.toEpochMilli()}",
            "notificationConfig":{
                "name":"name",
                "description":"description",
                "configType":"Slack",
                "features":["IndexManagement"],
                "isEnabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config-Id",
            lastUpdatedTimeMs,
            createdTimeMs,
            "selectedTenant",
            sampleConfig
        )
        val jsonString = """
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
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        assertEquals(configInfo, recreatedObject)
    }

    @Test
    fun `Config info should throw exception if configId is empty`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            slack = sampleSlack
        )
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfigInfo(
                "",
                Instant.now(),
                Instant.now(),
                "tenant",
                sampleConfig
            )
        }
    }

    @Test
    fun `Config info should throw exception if configId is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
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
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if lastUpdatedTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "configId":"config-Id",
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
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if createdTimeMs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val jsonString = """
        {
            "configId":"config-Id",
            "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
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
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `Config info should throw exception if notificationConfig is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "configId":"config-Id",
            "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
            "createdTimeMs":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }
}
