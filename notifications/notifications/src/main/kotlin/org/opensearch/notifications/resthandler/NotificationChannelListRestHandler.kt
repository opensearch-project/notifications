/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.action.RestToXContentListener

/**
 * Rest handler for getting notification channels.
 */
internal class NotificationChannelListRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/channels"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return "notifications_channel_list"
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Get notification channels
             * Request URL: GET [REQUEST_URL]
             * Request body: Ref [org.opensearch.commons.notifications.action.GetChannelListRequest]
             * Response body: [org.opensearch.commons.notifications.action.GetChannelListResponse]
             */
            Route(GET, REQUEST_URL)
        )
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
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            GET -> {
                Metrics.NOTIFICATIONS_CHANNELS_INFO_TOTAL.counter.increment()
                Metrics.NOTIFICATIONS_CHANNELS_INFO_INTERVAL_COUNT.counter.increment()
                RestChannelConsumer {
                    NotificationsPluginInterface.getChannelList(
                        client,
                        GetChannelListRequest(),
                        RestToXContentListener(it)
                    )
                }
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
