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
 * This class helps in fetching the right Channel Factory based on the
 * type of the channel.
 * A channel could be Email, Webhook etc
 */
internal object DestinationFactoryProvider {

    var destinationFactoryMap = mapOf(
        // TODO Add other channel
        "Slack" to WebhookDestinationFactory(),
        "Chime" to WebhookDestinationFactory(),
        "Webhook" to WebhookDestinationFactory()
    )

    /**
     * Fetches the right channel factory based on the type of the channel
     *
     * @param destinationType [{@link DestinationType}]
     * @return DestinationFactory factory object for above destination type
     */
    fun getFactory(destinationType: String): DestinationFactory<BaseDestination> {
        require(destinationFactoryMap.containsKey(destinationType)) { "Invalid channel type" }
        return destinationFactoryMap[destinationType] as DestinationFactory<BaseDestination>
    }
}
