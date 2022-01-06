/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
