/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination
/*
This class holds the contents of a Telegram  destination
 */
class TelegramDestination(
    val token: String,
    val chatId: Long,
    val url: String = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId"
) : WebhookDestination(url, DestinationType.TELEGRAM)
