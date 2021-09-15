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

package org.opensearch.notifications.core.transport

import org.opensearch.notifications.core.spi.model.destination.BaseDestination
import org.opensearch.notifications.core.spi.model.destination.DestinationType
import org.opensearch.notifications.core.utils.OpenForTesting

/**
 * This class helps in fetching the right destination transport based on type
 * A Destination could be SMTP, Webhook etc
 */
internal object DestinationTransportProvider {

    private val webhookDestinationTransport = WebhookDestinationTransport()
    private val smtpDestinationTransport = SmtpDestinationTransport()
    private val snsDestinationTransport = SnsDestinationTransport()
    private val sesDestinationTransport = SesDestinationTransport()

    @OpenForTesting
    var destinationTransportMap = mapOf(
        DestinationType.SLACK to webhookDestinationTransport,
        DestinationType.CHIME to webhookDestinationTransport,
        DestinationType.CUSTOM_WEBHOOK to webhookDestinationTransport,
        DestinationType.SMTP to smtpDestinationTransport,
        DestinationType.SNS to snsDestinationTransport,
        DestinationType.SES to sesDestinationTransport
    )

    /**
     * Fetches the right destination transport based on the type
     *
     * @param destinationType [{@link DestinationType}]
     * @return DestinationTransport transport object for above destination type
     */
    @Suppress("UNCHECKED_CAST")
    fun getTransport(destinationType: DestinationType): DestinationTransport<BaseDestination> {
        val retVal = destinationTransportMap[destinationType] ?: throw IllegalArgumentException("Invalid channel type")
        return retVal as DestinationTransport<BaseDestination>
    }
}
