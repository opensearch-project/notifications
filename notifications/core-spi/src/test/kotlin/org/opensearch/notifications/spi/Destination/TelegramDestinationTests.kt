/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.Destination
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.TelegramDestination
class TelegramDestinationTests {
    fun testTelegramDestination() {
        val token = "5910159857:AAF9qpqgPp3SZE_gAsKvhjy0uVgJ9zLLe7"
        val url = "https://t.me/notificationPluginBot"
        val destination = TelegramDestination(token, url)
        assert(destination.token == token) { "Token assertion failed" }
        assert(destination.url == url) { "URL assertion failed" }
        assert(destination.destinationType == DestinationType.TELEGRAM) { "Destination type assertion failed" }

        // Test sendTelegramMessage function
        val message = "Hello World"
        destination.sendMessage(token, url, message, messageKey)
        assert(response.isSuccessful) { "Failed to send Telegram message, response code: ${response.code()}" }
    }
}
