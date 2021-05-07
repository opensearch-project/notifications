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
import java.time.Instant
import java.util.EnumSet

internal class NotificationConfigInfoTests {

    @Test
    fun `Config info serialize and deserialize with config object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
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
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
        )
        val configInfo = NotificationConfigInfo(
            "config_id",
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
            ConfigType.SLACK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
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
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "notification_config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
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
            ConfigType.SLACK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
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
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant",
            "notification_config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
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
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
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
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant",
            "notification_config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
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
            "config_id":"config-Id",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant",
            "notification_config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
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
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant",
            "notification_config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
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
            "config_id":"config-Id",
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant"
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }
}
