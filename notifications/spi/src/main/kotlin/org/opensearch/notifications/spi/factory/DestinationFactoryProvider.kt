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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
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
        "Slack" to WebhookDestinationFactory(),
        "Chime" to WebhookDestinationFactory(),
        "Webhook" to WebhookDestinationFactory()
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
}
