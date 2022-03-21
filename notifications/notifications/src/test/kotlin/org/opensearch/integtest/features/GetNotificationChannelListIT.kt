/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.features

import com.google.gson.JsonObject
import org.junit.Assert
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import kotlin.random.Random

class GetNotificationChannelListIT : PluginRestTestCase() {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun getCreateRequestJsonString(
        nameSubstring: String,
        configType: ConfigType,
        isEnabled: Boolean,
        smtpAccountId: String = "",
        emailGroupId: Set<String> = setOf()
    ): String {
        val randomString = (1..20)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
        val configObjectString = when (configType) {
            ConfigType.SLACK -> """
                "slack":{"url":"https://slack.domain.com/sample_slack_url#$randomString"}
            """.trimIndent()
            ConfigType.CHIME -> """
                "chime":{"url":"https://chime.domain.com/sample_chime_url#$randomString"}
            """.trimIndent()
            ConfigType.WEBHOOK -> """
                "webhook":{"url":"https://web.domain.com/sample_web_url#$randomString"}
            """.trimIndent()
            ConfigType.SMTP_ACCOUNT -> """
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"4321",
                    "method":"ssl",
                    "from_address":"$randomString@from.com"
                }
            """.trimIndent()
            ConfigType.EMAIL_GROUP -> """
                "email_group":{
                    "recipient_list":[
                        {"recipient":"$randomString+recipient1@from.com"},
                        {"recipient":"$randomString+recipient2@from.com"}
                    ]
                }
            """.trimIndent()
            ConfigType.EMAIL -> """
                "email":{
                    "email_account_id":"$smtpAccountId",
                    "recipient_list":[{"recipient":"$randomString@from.com"}],
                    "email_group_id_list":[${emailGroupId.joinToString { "\"$it\"" }}]
                }
            """.trimIndent()
            else -> throw IllegalArgumentException("Unsupported configType=$configType")
        }
        return """
        {
            "config_id":"$randomString",
            "config":{
                "name":"$nameSubstring:this is a sample config name $randomString",
                "description":"this is a sample config description $randomString",
                "config_type":"$configType",
                "is_enabled":$isEnabled,
                $configObjectString
            }
        }
        """.trimIndent()
    }

    private fun createConfig(
        nameSubstring: String = "",
        configType: ConfigType = ConfigType.SLACK,
        isEnabled: Boolean = true,
        smtpAccountId: String = "",
        emailGroupId: Set<String> = setOf()
    ): String {
        val createRequestJsonString = getCreateRequestJsonString(
            nameSubstring,
            configType,
            isEnabled,
            smtpAccountId,
            emailGroupId
        )
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(100)
        return configId
    }

    private fun verifyChannelIdEquals(idSet: Set<String>, jsonObject: JsonObject, totalHits: Int = -1) {
        if (totalHits >= 0) {
            Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
        }
        val items = jsonObject.get("channel_list").asJsonArray
        Assert.assertEquals(idSet.size, items.size())
        items.forEach {
            val item = it.asJsonObject
            val configId = item.get("config_id").asString
            Assert.assertNotNull(configId)
            Assert.assertTrue(idSet.contains(configId))
        }
    }

    fun `test POST channel list should result in error`() {
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/channels",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test PUT channel list should result in error`() {
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/channels",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test getChannelList should return only channels`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val emailId = createConfig(
            configType = ConfigType.EMAIL,
            smtpAccountId = smtpAccountId,
            emailGroupId = setOf(emailGroupId)
        )
        Thread.sleep(1000)

        val channelIds = setOf(slackId, chimeId, webhookId, emailId)
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/channels",
            "",
            RestStatus.OK.status
        )
        Thread.sleep(100)
        verifyChannelIdEquals(channelIds, response, channelIds.size)
        Thread.sleep(100)
    }
}
