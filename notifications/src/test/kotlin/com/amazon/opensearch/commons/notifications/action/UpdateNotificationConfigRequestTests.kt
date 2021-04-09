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
package com.amazon.opensearch.commons.notifications.action

import com.amazon.opensearch.commons.notifications.model.Chime
import com.amazon.opensearch.commons.notifications.model.ConfigType
import com.amazon.opensearch.commons.notifications.model.Email
import com.amazon.opensearch.commons.notifications.model.EmailGroup
import com.amazon.opensearch.commons.notifications.model.Feature
import com.amazon.opensearch.commons.notifications.model.NotificationConfig
import com.amazon.opensearch.commons.notifications.model.Slack
import com.amazon.opensearch.commons.notifications.model.SmtpAccount
import com.amazon.opensearch.commons.notifications.model.Webhook
import com.amazon.opensearch.commons.utils.createObjectFromJsonString
import com.amazon.opensearch.commons.utils.getJsonString
import com.amazon.opensearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.EnumSet

internal class UpdateNotificationConfigRequestTests {

    private fun createAllContentConfigObject(): NotificationConfig {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val sampleEmail = Email("id_1234567890", listOf("email@domain.com"), listOf("groupId"))
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val sampleEmailGroup = EmailGroup(listOf("email@domain.com"))
        return NotificationConfig(
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
    }

    @Test
    fun `Update config serialize and deserialize transport object should be equal`() {
        val configRequest = UpdateNotificationConfigRequest("configId", createAllContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { UpdateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("configId", recreatedObject.configId)
    }

    @Test
    fun `Update config serialize and deserialize using json object should be equal`() {
        val configRequest = UpdateNotificationConfigRequest("configId", createAllContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
        assertEquals("configId", recreatedObject.configId)
    }

    @Test
    fun `Update config should deserialize json object using parser`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )

        val jsonString = """
        {
            "configId":"configId1",
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
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("configId1", recreatedObject.configId)
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
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )

        val jsonString = """
        {
            "configId":"configId1",
            "notificationConfig":{
                "name":"name",
                "description":"description",
                "configType":"Slack",
                "features":["IndexManagement"],
                "isEnabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
        assertEquals("configId1", recreatedObject.configId)
    }
}
