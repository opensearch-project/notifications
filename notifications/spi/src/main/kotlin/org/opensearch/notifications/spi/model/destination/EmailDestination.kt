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

import org.opensearch.common.Strings
import org.opensearch.notifications.spi.utils.validateEmail

class EmailDestination(
    val host: String,
    val port: Int,
    val method: String,
    val fromAddress: String,
    val recipient: String,
    destinationType: String,
) : BaseDestination(destinationType) {

    init {
        require(!Strings.isNullOrEmpty(host)) { "host is null or empty" }
        require(port > 0) { "port should be positive value" }
        validateEmail(fromAddress)
    }
}