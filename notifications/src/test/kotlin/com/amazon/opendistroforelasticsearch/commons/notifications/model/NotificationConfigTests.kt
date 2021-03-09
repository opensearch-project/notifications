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
import com.amazon.opendistroforelasticsearch.notifications.recreateObject
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

internal class NotificationConfigTests : ESTestCase() {

    @Test
    fun `Config serialize and deserialize with slack object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Slack,
            EnumSet.of(NotificationConfig.Feature.Reports),
            slack = sampleSlack
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize using json slack object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Slack,
            EnumSet.of(NotificationConfig.Feature.Reports),
            slack = sampleSlack
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        ESTestCase.assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with chime object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Chime,
            EnumSet.of(NotificationConfig.Feature.Alerting),
            chime = sampleChime
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json chime object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Chime,
            EnumSet.of(NotificationConfig.Feature.Alerting),
            chime = sampleChime
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with webhook object should be equal`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            webhook = sampleWebhook
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json webhook object should be equal`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            webhook = sampleWebhook
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with multiple objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with multiple json objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with disabled multiple objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = false,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with disabled multiple json objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = false,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "configType":"Webhook",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"},
            "extra_field":"extra value"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config should safely ignore unknown config type in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.None,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "configType":"NewConfig",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"},
            "newConfig":{"newField":"new value"}
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config should safely ignore unknown feature type in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement, NotificationConfig.Feature.None),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "configType":"Webhook",
            "features":["IndexManagement", "NewFeature"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"}
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config throw exception if slack object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                NotificationConfig.ConfigType.Slack,
                EnumSet.of(NotificationConfig.Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if chime object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                NotificationConfig.ConfigType.Chime,
                EnumSet.of(NotificationConfig.Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if webhook object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                NotificationConfig.ConfigType.Webhook,
                EnumSet.of(NotificationConfig.Feature.IndexManagement)
            )
        }
    }
}
