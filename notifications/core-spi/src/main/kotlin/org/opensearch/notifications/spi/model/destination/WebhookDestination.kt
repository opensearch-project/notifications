/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

import org.opensearch.notifications.spi.utils.validateUrl

/**
 * This class holds the contents of generic webbook destination
 */
abstract class WebhookDestination(
    val url: String,
    destinationType: DestinationType
) : BaseDestination(destinationType) {

    init {
        validateUrl(url)
    }

    override fun toString(): String {
        return "DestinationType: $destinationType , Url: $url"
    }
}
