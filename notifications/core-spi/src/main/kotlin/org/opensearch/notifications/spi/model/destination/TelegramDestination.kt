/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination
/*
This class holds the contents of a Telegram  destination
 */
class TelegramDestination(
    url: String
) : WebhookDestination(url, DestinationType.TELEGRAM)
