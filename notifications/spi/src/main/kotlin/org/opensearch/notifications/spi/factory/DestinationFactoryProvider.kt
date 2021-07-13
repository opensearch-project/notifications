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

package org.opensearch.notifications.spi.factory

import org.opensearch.notifications.spi.model.destination.BaseDestination

/**
 * This class helps in fetching the right destination factory based on type
 * A Destination could be Email, Webhook etc
 */
internal object DestinationFactoryProvider {

    var destinationFactoryMap = mapOf(
        // TODO Add other channel
        "slack" to WebhookDestinationFactory(),
        "chime" to WebhookDestinationFactory(),
        "webhook" to WebhookDestinationFactory(),
        "smtp" to SmtpEmailDestinationFactory()
    )

    /**
     * Fetches the right destination factory based on the type
     *
     * @param destinationType [{@link DestinationType}]
     * @return DestinationFactory factory object for above destination type
     */
    fun getFactory(destinationType: String): DestinationFactory<BaseDestination> {
        require(destinationFactoryMap.containsKey(destinationType)) { "Invalid channel type" }
        return destinationFactoryMap[destinationType] as DestinationFactory<BaseDestination>
    }

    /**
     * Fetch the allowed destinations
     *
     * @return List<String> list of allowed destinations
     */
    fun getAllowedDestinationList(): List<String> {
        return destinationFactoryMap.keys.toList()
    }
}
