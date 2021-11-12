/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

import org.opensearch.notifications.spi.utils.validateMethod

/**
 * This class holds the contents of a custom webhook destination
 */
class CustomWebhookDestination(
    url: String,
    val headerParams: Map<String, String>,
    val method: String
) : WebhookDestination(url, DestinationType.CUSTOM_WEBHOOK) {

    init {
        validateMethod(method)
    }
}
