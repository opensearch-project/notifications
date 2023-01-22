/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.model.destination
import org.opensearch.notifications.spi.utils.validateUrl
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TelegramDestination(val token: String, url: String) : WebhookDestination(url, DestinationType.TELEGRAM) {
    init {
        validateUrl(url)
    }
    fun sendMessage(token: String, url: String, message: String, messageKey: String) {
        val telegramUrl = "https://api.telegram.org/bot$token/sendMessage"
        val connectionUrl = URL(telegramUrl)
        val connection = connectionUrl.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setDoOutput(true)

        val json = """
            {
                "chat_id": "$url",
                "text": "$message",
                "parse_mode": "Markdown",
                "disable_web_page_preview": true,
                "message_key": "$messageKey"
            }
        """.trimIndent()

        val dataOutputStream = DataOutputStream(connection.outputStream)
        dataOutputStream.writeBytes(json)
        dataOutputStream.flush()
        dataOutputStream.close()
        val response = connection.responseCode
        val input = BufferedReader(InputStreamReader(connection.inputStream))
        var inputLine: String?
        val responseBuilder = StringBuilder()
        inputLine = input.readLine()
        while (inputLine != null) {
            responseBuilder.append(inputLine)
            inputLine = input.readLine()
        }
        input.close()
        println("Response  Code: $response")
        println("Response: $responseBuilder")
    }
}
