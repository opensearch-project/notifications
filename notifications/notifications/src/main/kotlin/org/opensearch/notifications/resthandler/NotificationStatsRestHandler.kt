/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.notifications.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestStatus

/**
 * Rest handler for getting notifications backend stats
 */
internal class NotificationStatsRestHandler : BaseRestHandler() {
    companion object {
        private const val NOTIFICATION_STATS_ACTION = "notification_stats"
        private const val NOTIFICATION_STATS_URL = "$PLUGIN_BASE_URI/_local/stats"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return NOTIFICATION_STATS_ACTION
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            // TODO: Wrap this into TransportAction
            GET -> RestChannelConsumer {
                // it.sendResponse(BytesRestResponse(RestStatus.OK, Metrics.getInstance().collectToFlattenedJSON()))
                it.sendResponse(BytesRestResponse(RestStatus.OK, Metrics.collectToFlattenedJSON()))
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
