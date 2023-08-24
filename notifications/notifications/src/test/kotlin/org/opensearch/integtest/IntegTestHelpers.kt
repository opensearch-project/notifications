/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.opensearch.client.Response
import org.opensearch.commons.notifications.model.ConfigType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val DEFAULT_TIME_ACCURACY_SEC = 5L

@Throws(IOException::class)
@Suppress("NestedBlockDepth")
fun getResponseBody(response: Response, retainNewLines: Boolean = true): String {
    val sb = StringBuilder()
    response.entity.content.use { `is` ->
        BufferedReader(
            InputStreamReader(`is`, StandardCharsets.UTF_8)
        ).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
                if (retainNewLines) {
                    sb.appendLine()
                }
            }
        }
    }
    return sb.toString()
}

fun jsonify(text: String): JsonObject {
    return JsonParser.parseString(text).asJsonObject
}

fun validateTimeNearRefTime(time: Instant, refTime: Instant, accuracySeconds: Long) {
    assertTrue(time.plusSeconds(accuracySeconds).isAfter(refTime), "$time + $accuracySeconds > $refTime")
    assertTrue(time.minusSeconds(accuracySeconds).isBefore(refTime), "$time - $accuracySeconds < $refTime")
}

fun validateTimeRecency(time: Instant, accuracySeconds: Long = DEFAULT_TIME_ACCURACY_SEC) {
    validateTimeNearRefTime(time, Instant.now(), accuracySeconds)
}

fun validateErrorResponse(response: JsonObject, statusCode: Int, errorType: String = "status_exception") {
    Assert.assertNotNull("Error response content should be generated", response)
    val status = response.get("status").asInt
    val error = response.get("error").asJsonObject
    val rootCause = error.get("root_cause").asJsonArray
    val type = error.get("type").asString
    val reason = error.get("reason").asString
    Assert.assertEquals(statusCode, status)
    Assert.assertEquals(errorType, type)
    Assert.assertNotNull(reason)
    Assert.assertNotNull(rootCause)
    Assert.assertTrue(rootCause.size() > 0)
}

fun verifyResponse(response: JsonObject, refTag: String, recipients: List<String>) {
    // verify ref tag is consistent
    val actualRefTag = response.get("ref_tag").asString
    assertEquals(refTag, actualRefTag)

    // verify status to each recipient
    val actualRecipients = response.getAsJsonArray("recipient_list")
    actualRecipients.forEach { item ->
        val recipient = item.asJsonObject.get("recipient").asString
        assert(recipients.contains(recipient))

        val statusCode = item.asJsonObject.get("status_code").asInt
        assertEquals(200, statusCode)

        val statusText = item.asJsonObject.get("status_text").asString
        assertEquals("Success", statusText)
    }
}

fun getStatusCode(response: JsonObject): Int {
    return response
        .getAsJsonArray("recipient_list")
        .get(0).asJsonObject
        .get("status_code").asInt
}

fun getStatusText(response: JsonObject): String {
    return response
        .getAsJsonArray("recipient_list")
        .get(0).asJsonObject
        .get("status_text").asString
}

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun getCreateNotificationRequestJsonString(
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
        ConfigType.MICROSOFT_TEAMS -> """
            "microsoft_teams":{"url":"https://microsoftTeams.domain.webhook.office.com/sample_microsoft_teams_url#$randomString"}
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

/** Util class to build Json entity of request body */
class NotificationsJsonEntity(
    private val refTag: String?,
    private val recipients: List<String>,
    private val title: String?,
    private val textDescription: String?,
    private val htmlDescription: String?,
    private val attachment: String?
) {

    var jsonEntityString: String = ""

    private constructor(builder: Builder) : this(
        builder.refTag,
        builder.recipients,
        builder.title,
        builder.textDescription,
        builder.htmlDescription,
        builder.attachment
    )

    fun getJsonEntityAsString(): String {
        updateJsonEntity()
        return jsonEntityString
    }

    private fun updateJsonEntity() {
        jsonEntityString =
            """
            {
              "ref_tag": "$refTag",
              "recipient_list": ${listToString(recipients)},
              "title": "$title",
              "text_description": "$textDescription",
              "html_description": "$htmlDescription",
              "attachment": $attachment
            }
            """.trimIndent()
    }

    private fun listToString(list: List<String>): String {
        return list.joinToString("\", \"", "[\"", "\"]")
    }

    class Builder {
        var refTag: String? = null
            private set
        var recipients: List<String> = emptyList()
            private set
        var title: String? = null
            private set
        var textDescription: String? = null
            private set
        var htmlDescription: String? = null
            private set
        var attachment: String? = null
            private set

        fun setRefTag(refTag: String?) = apply { this.refTag = refTag }

        fun setRecipients(recipients: List<String>) = apply { this.recipients = recipients }

        fun setTitle(title: String) = apply { this.title = title }

        fun setTextDescription(textDescription: String) = apply { this.textDescription = textDescription }

        fun setHtmlDescription(htmlDescription: String) = apply { this.htmlDescription = htmlDescription }

        fun setAttachment(attachment: String) = apply { this.attachment = attachment }

        fun build() = NotificationsJsonEntity(this)
    }
}
