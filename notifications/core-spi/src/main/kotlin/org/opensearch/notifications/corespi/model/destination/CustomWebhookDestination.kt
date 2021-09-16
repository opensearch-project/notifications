/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.corespi.model.destination

import org.opensearch.notifications.corespi.utils.validateMethod

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
