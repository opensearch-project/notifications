/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.transport.client.node.NodeClient

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
    override fun getName(): String = NOTIFICATION_STATS_ACTION

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> =
        listOf(
            /*
             * Get Notifications Stats
             * Request body: None
             * TODO: Add response body in common-utils
             */
            Route(GET, NOTIFICATION_STATS_URL),
        )

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> = setOf()

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(
        request: RestRequest,
        client: NodeClient,
    ): RestChannelConsumer =
        when (request.method()) {
            // TODO: Wrap this into TransportAction
            GET -> {
                RestChannelConsumer {
                    it.sendResponse(BytesRestResponse(RestStatus.OK, Metrics.collectToFlattenedJSON()))
                }
            }

            else -> {
                RestChannelConsumer {
                    it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
                }
            }
        }
}
