/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.model.destination

import org.opensearch.notifications.spi.utils.validateUrl
class TelegramDestination(val token: String, url: String) : WebhookDestination(url, DestinationType.TELEGRAM) {
    init {
        validateUrl(url)
    }
}
