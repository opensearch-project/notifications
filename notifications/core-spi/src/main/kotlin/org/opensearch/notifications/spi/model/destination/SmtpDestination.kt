/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

import org.opensearch.core.common.Strings
import org.opensearch.notifications.spi.utils.validateEmail

/**
 * This class holds the contents of smtp destination
 */
class SmtpDestination(
    val accountName: String,
    val host: String,
    val port: Int,
    val method: String,
    val fromAddress: String,
    val recipient: String,
) : BaseDestination(DestinationType.SMTP) {
    init {
        require(!Strings.isNullOrEmpty(host)) { "Host name should be provided" }
        require(port > 0) { "Port should be positive value" }
        validateEmail(fromAddress)
        validateEmail(recipient)
    }
}
