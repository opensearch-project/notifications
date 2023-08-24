/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.transport

import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.spi.model.destination.BaseDestination
import org.opensearch.notifications.spi.model.destination.DestinationType

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
        DestinationType.MICROSOFT_TEAMS to webhookDestinationTransport,
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
