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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import java.util.EnumSet

internal class NotificationConfigTests {

    @Test
    fun `Config serialize and deserialize with slack object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize using json slack object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.REPORTS),
            configData = sampleSlack
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with chime object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            EnumSet.of(Feature.ALERTING),
            configData = sampleChime
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json chime object should be equal`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            EnumSet.of(Feature.ALERTING),
            configData = sampleChime
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
            "description",
            ConfigType.WEBHOOK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleWebhook
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json webhook object should be equal`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleWebhook
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with email object should be equal`() {
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleEmail
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json email object should be equal`() {
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleEmail
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json smtpAccount object should be equal`() {
        val smtpAccount = SmtpAccount("domain.com", 1234, MethodType.SSL, "from@domain.com")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = smtpAccount
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with smtpAccount object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, MethodType.SSL, "from@domain.com")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleSmtpAccount
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json emailGroup object should be equal`() {
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleEmailGroup
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with emailGroup object should be equal`() {
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            configData = sampleEmailGroup
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test

    fun `Config should safely ignore unknown config type in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.NONE,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
        )
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "config_type":"NewConfig",
            "feature_list":["index_management"],
            "is_enabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"},
            "new_config1":{"newField1":"new value 1"},
            "new_config2":{"newField2":"new value 2"}
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config should safely ignore unknown feature type in json object`() {
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            EnumSet.of(Feature.INDEX_MANAGEMENT, Feature.NONE),
            isEnabled = true,
            configData = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "config_type":"webhook",
            "feature_list":["index_management", "NewFeature1", "NewFeature2"],
            "is_enabled":true,
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"}
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }
}
