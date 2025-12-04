/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.action.RestToXContentListener
import org.opensearch.transport.client.node.NodeClient

/**
 * Rest handler for getting notification features.
 */
internal class NotificationFeaturesRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/features"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String = "notifications_features"

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> =
        listOf(
            /*
             * Get notification features
             * Request URL: GET [REQUEST_URL]
             * Request body: Ref [org.opensearch.commons.notifications.action.GetPluginFeaturesRequest]
             * Response body: [org.opensearch.commons.notifications.action.GetPluginFeaturesResponse]
             */
            Route(GET, REQUEST_URL),
        )

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> = setOf()

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: RestRequest,
        client: NodeClient,
    ): RestChannelConsumer =
        when (request.method()) {
            GET -> {
                RestChannelConsumer {
                    Metrics.NOTIFICATIONS_FEATURES_INFO_TOTAL.counter.increment()
                    Metrics.NOTIFICATIONS_FEATURES_INFO_INTERVAL_COUNT.counter.increment()
                    NotificationsPluginInterface.getPluginFeatures(
                        client,
                        GetPluginFeaturesRequest(),
                        RestToXContentListener(it),
                    )
                }
            }

            else -> {
                RestChannelConsumer {
                    it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
                }
            }
        }
}
