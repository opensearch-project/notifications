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
import java.util.EnumSet

internal class NotificationConfigTests {

    @Test
    fun `Config serialize and deserialize with slack object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
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
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.Reports),
            slack = sampleSlack
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
            ConfigType.Chime,
            EnumSet.of(Feature.Alerting),
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
            "description",
            ConfigType.Chime,
            EnumSet.of(Feature.Alerting),
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
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
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
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            webhook = sampleWebhook
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
            ConfigType.Email,
            EnumSet.of(Feature.IndexManagement),
            email = sampleEmail
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
            ConfigType.Email,
            EnumSet.of(Feature.IndexManagement),
            email = sampleEmail
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with json smtpAccount object should be equal`() {
        val smtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SmtpAccount,
            EnumSet.of(Feature.IndexManagement),
            smtpAccount = smtpAccount
        )
        val jsonString = getJsonString(sampleConfig)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfig.parse(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with smtpAccount object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.SmtpAccount,
            EnumSet.of(Feature.IndexManagement),
            smtpAccount = sampleSmtpAccount
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
            ConfigType.EmailGroup,
            EnumSet.of(Feature.IndexManagement),
            emailGroup = sampleEmailGroup
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
            ConfigType.EmailGroup,
            EnumSet.of(Feature.IndexManagement),
            emailGroup = sampleEmailGroup
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with multiple objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook,
            email = sampleEmail,
            smtpAccount = sampleSmtpAccount,
            emailGroup = sampleEmailGroup
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with multiple json objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook,
            email = sampleEmail,
            smtpAccount = sampleSmtpAccount,
            emailGroup = sampleEmailGroup
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
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = false,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook,
            email = sampleEmail,
            smtpAccount = sampleSmtpAccount,
            emailGroup = sampleEmailGroup
        )
        val recreatedObject = recreateObject(sampleConfig) { NotificationConfig(it) }
        assertEquals(sampleConfig, recreatedObject)
    }

    @Test
    fun `Config serialize and deserialize with disabled multiple json objects should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        val sampleConfig = NotificationConfig(
            "name",
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = false,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook,
            email = sampleEmail,
            smtpAccount = sampleSmtpAccount,
            emailGroup = sampleEmailGroup
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
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "configType":"Webhook",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"},
            "extra_field_1":"extra value 1",
            "extra_field_2":"extra value 2"
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
            "description",
            ConfigType.None,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "configType":"NewConfig",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "chime":{"url":"https://domain.com/sample_chime_url#1234567890"},
            "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"},
            "newConfig1":{"newField1":"new value 1"},
            "newConfig2":{"newField2":"new value 2"}
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
            "description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement, Feature.None),
            isEnabled = true,
            slack = sampleSlack,
            chime = sampleChime,
            webhook = sampleWebhook
        )
        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "configType":"Webhook",
            "features":["IndexManagement", "NewFeature1", "NewFeature2"],
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
                "description",
                ConfigType.Slack,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if chime object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                "description",
                ConfigType.Chime,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if webhook object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                "description",
                ConfigType.Webhook,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if email object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                "description",
                ConfigType.Email,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if smtpAccount object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                "description",
                ConfigType.SmtpAccount,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }

    @Test
    fun `Config throw exception if emailGroup object is absent`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            NotificationConfig(
                "name",
                "description",
                ConfigType.EmailGroup,
                EnumSet.of(Feature.IndexManagement)
            )
        }
    }
}
