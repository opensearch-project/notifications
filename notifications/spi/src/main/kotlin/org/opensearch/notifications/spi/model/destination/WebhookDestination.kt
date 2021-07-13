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

import org.apache.http.client.utils.URIBuilder
import org.opensearch.common.Strings
import org.opensearch.notifications.spi.utils.validateUrl
import java.net.URI
import java.net.URISyntaxException

/**
 * This class holds the contents of generic webbook destination
 */
abstract class WebhookDestination(
    val url: String,
    destinationType: DestinationType
) : BaseDestination(destinationType) {

    init {
        require(!Strings.isNullOrEmpty(url)) { "url is invalid or empty" }
        validateUrl(url)
    }

    @SuppressWarnings("SwallowedException")
    internal fun buildUri(): URI {
        return try {
            URIBuilder(url).build()
        } catch (exception: URISyntaxException) {
            throw IllegalStateException("Error creating URI")
        }
    }

    override fun toString(): String {
        return "DestinationType: $destinationType , Url: $url"
    }
}
