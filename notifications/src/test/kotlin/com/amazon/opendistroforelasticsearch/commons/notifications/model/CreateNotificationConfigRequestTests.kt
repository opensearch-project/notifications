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

import com.amazon.opendistroforelasticsearch.commons.notifications.action.CreateNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.notifications.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.notifications.getJsonString
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.EnumSet

internal class CreateNotificationConfigRequestTests : ESTestCase() {

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
            NotificationConfig.ConfigType.Webhook,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
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
    fun `Create config serialize and deserialize transport object should be equal`() {
        val configRequest = CreateNotificationConfigRequest(createAllContentConfigObject())
        val recreatedObject =
            recreateObject(configRequest) { CreateNotificationConfigRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config serialize and deserialize using json object should be equal`() {
        val configRequest = CreateNotificationConfigRequest(createAllContentConfigObject())
        val jsonString = getJsonString(configRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(configRequest.notificationConfig, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should deserialize json object using parser`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            NotificationConfig.ConfigType.Slack,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )

        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "configType":"Slack",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }

    @Test
    fun `Create config should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        }
    }

    @Test
    fun `Create config should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val config = NotificationConfig(
            "name",
            "description",
            NotificationConfig.ConfigType.Slack,
            EnumSet.of(NotificationConfig.Feature.IndexManagement),
            isEnabled = true,
            slack = sampleSlack
        )

        val jsonString = """
        {
            "name":"name",
            "description":"description",
            "configType":"Slack",
            "features":["IndexManagement"],
            "isEnabled":true,
            "slack":{"url":"https://domain.com/sample_slack_url#1234567890"},
            "extra_field_1":["value 1", "value 2"],
            "extra_field_2":"extra value 2"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateNotificationConfigRequest.parse(it) }
        assertEquals(config, recreatedObject.notificationConfig)
    }
}
