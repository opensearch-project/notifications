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

package com.amazon.opendistroforelasticsearch.notifications.channel

import com.amazon.opendistroforelasticsearch.notifications.NotificationsRestTestCase
import com.amazon.opendistroforelasticsearch.notifications.jsonify
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import com.amazon.opendistroforelasticsearch.notifications.verifyResponse
import com.google.gson.JsonObject
import org.elasticsearch.rest.RestStatus

internal class SmtpChannelIT : NotificationsRestTestCase() {
    private val refTag = "sample raf name"
    private val title = "sample title"
    private val textDescription = "Description for notification in text"
    private val htmlDescription = "Description for notification in json encode html format"
    private val attachment = jsonify(
        """
        {
          "fileName": "odfe.data",
          "fileEncoding": "base64",
          "fileContentType": "application/octet-stream",
          "fileData": "VGVzdCBtZXNzYWdlCgo="
        }
        """.trimIndent())

    fun `test send email to one recipient over Smtp server`() {
        val recipients = listOf("mailto:test@localhost")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)
        verifyResponse(response, refTag, recipients)
    }

    fun `test send email to multiple recipient over Smtp server`() {
        val recipients = listOf("mailto:test1@localhost", "mailto:test2@abc.com", "mailto:test3@123.com")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)
        verifyResponse(response, refTag, recipients)
    }

    fun `test send email with unconfigured address`() {
        updateFromAddress(PluginSettings.UNCONFIGURED_EMAIL_ADDRESS)
        val recipients = listOf("mailto:test@localhost")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)

        val statusCode = getStatusCode(response)
        assertEquals(RestStatus.NOT_IMPLEMENTED.status, statusCode)

        val statusText = getStatusText(response)
        assertEquals("Email from: address not configured", statusText)
        resetFromAddress()
    }

    /** Private test util to extract status code from response of the first recipient */
    private fun getStatusCode(response: JsonObject): Int {
        return response
            .getAsJsonArray("recipients")
            .get(0).asJsonObject
            .get("statusCode").asInt
    }

    /** Private test util to extract status text from response of the first recipient */
    private fun getStatusText(response: JsonObject): String {
        return response
            .getAsJsonArray("recipients")
            .get(0).asJsonObject
            .get("statusText").asString
    }
}