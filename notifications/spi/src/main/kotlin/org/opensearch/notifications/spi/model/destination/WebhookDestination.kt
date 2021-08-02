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

package org.opensearch.notifications.spi.model.destination

import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.validateUrl

/**
 * This class holds the contents of generic webbook destination
 */
abstract class WebhookDestination(
    val url: String,
    destinationType: DestinationType
) : BaseDestination(destinationType) {

    init {
        validateUrl(url, PluginSettings.hostDenyList)
    }

    override fun toString(): String {
        return "DestinationType: $destinationType , Url: $url"
    }
}
