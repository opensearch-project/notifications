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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject
import java.util.EnumSet

internal class UpdateNotificationConfigRequestTests {

    private fun createWebhookContentConfigObject(): NotificationConfig {
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleWebhook
        )
    }

    private fun createSlackContentConfigObject(): NotificationConfig {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
        )
    }

    private fun createChimeContentConfigObject(): NotificationConfig {
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        return NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleChime
        )
    }

    private fun createEmailGroupContentConfigObject(): NotificationConfig {
        val sampleEmailGroup = EmailGroup(listOf("dummy@company.com"))
        return NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleEmailGroup
        )
    }

    private fun createEmailContentConfigObject(): NotificationConfig {
        val sampleEmail = Email(
            emailAccountID = "sample_1@dummy.com",
            recipients = listOf("sample_2@dummy.com"),
            emailGroupIds = listOf("sample_3@dummy.com")
        )
        return NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleEmail
        )
    }

    private fun createSmtpAccountContentConfigObject(): NotificationConfig {
        val sampleSmtpAccount = SmtpAccount(
            host = "http://dummy.com",
            port = 11,
            method = MethodType.SSL,
            fromAddress = "sample@dummy.com"
        )
        return NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSmtpAccount
        )
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal Webhook`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createWebhookContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal Slack`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createSlackContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal Chime`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createChimeContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal Email`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createEmailContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal EmailGroup`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createEmailGroupContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal SmtpAccount`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createSmtpAccountContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal webhook`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createWebhookContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal slack`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createSlackContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal Email`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createEmailContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal EmailGroup`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createEmailGroupContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal SmtpAccount`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createSmtpAccountContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal chime`() {
        val configRequest = UpdateNotificationConfigRequest("config_id", createChimeContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("config_id", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser slack`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser webhook`() {
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.WEBHOOK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleWebhook
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"webhook",
                "feature_list":["index_management"],
                "is_enabled":true,
                "webhook":{"url":"https://domain.com/sample_webhook_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser Chime`() {
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.CHIME,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleChime
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"chime",
                "feature_list":["index_management"],
                "is_enabled":true,
                "chime":{"url":"https://domain.com/sample_chime_url#1234567890"}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser Email Group`() {
        val sampleEmailGroup = EmailGroup(listOf("dummy@company.com"))
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL_GROUP,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleEmailGroup
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"email_group",
                "feature_list":["index_management"],
                "is_enabled":true,
                "email_group":{"recipient_list":["dummy@company.com"]}
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser Email`() {
        val sampleEmail = Email(
            emailAccountID = "sample_1@dummy.com",
            recipients = listOf("sample_2@dummy.com"),
            emailGroupIds = listOf("sample_3@dummy.com")
        )
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.EMAIL,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleEmail
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"email",
                "feature_list":["index_management"],
                "is_enabled":true,
                "email":{"email_account_id":"sample_1@dummy.com","recipient_list":["sample_2@dummy.com"],
                "email_group_id_list":["sample_3@dummy.com"] }
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser SmtpAccount`() {
        val sampleSmtpAccount = SmtpAccount(
            host = "http://dummy.com",
            port = 11,
            method = MethodType.SSL,
            fromAddress = "sample@dummy.com"
        )
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SMTP_ACCOUNT,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"smtp_account",
                "feature_list":["index_management"],
                "is_enabled":true,
                "smtp_account":{"host":"http://dummy.com", "port":11,"method": "ssl", "from_address": "sample@dummy.com" }
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }

    @Test
    fun `Update config should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        }
    }

    @Test
    fun `Update config should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            EnumSet.of(Feature.INDEX_MANAGEMENT),
            isEnabled = true,
            configData = sampleSlack
        )

        val jsonString = """
        {
            "config_id":"config_id1",
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "feature_list":["index_management"],
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("config_id1", recreatedObject.configId)
    }
}
