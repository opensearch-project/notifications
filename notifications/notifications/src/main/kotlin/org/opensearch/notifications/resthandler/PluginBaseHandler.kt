/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
package org.opensearch.notifications.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.RestRequest

/**
 * Base class for Rest handler for plugin.
 */
abstract class PluginBaseHandler : BaseRestHandler() {

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        Metrics.REQUEST_TOTAL.counter.increment()
        Metrics.REQUEST_INTERVAL_COUNT.counter.increment()
        return executeRequest(request, client)
    }

    /**
     * Execute the plugin rest request.
     */
    protected abstract fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer
}
