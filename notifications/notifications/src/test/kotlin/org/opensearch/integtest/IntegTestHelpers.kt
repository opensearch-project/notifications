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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.integtest

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.opensearch.client.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Instant
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
        builder.refTag, builder.recipients, builder.title,
        builder.textDescription, builder.htmlDescription, builder.attachment
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
