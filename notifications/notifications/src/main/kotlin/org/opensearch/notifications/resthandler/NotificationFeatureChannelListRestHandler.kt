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
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_TAG
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener

/**
 * Rest handler for getting notification channels for a feature.
 */
internal class NotificationFeatureChannelListRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/feature/channels"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return "notifications_feature_channel_list"
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Get a notification event
             * Request URL: GET [REQUEST_URL/{feature}]
             * Request body: Ref [org.opensearch.commons.notifications.action.GetFeatureChannelListRequest]
             * Response body: [org.opensearch.commons.notifications.action.GetFeatureChannelListResponse]
             */
            Route(GET, "$REQUEST_URL/{$FEATURE_TAG}")
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
                val feature = request.param(FEATURE_TAG)
                RestChannelConsumer {
                    NotificationsPluginInterface.getFeatureChannelList(
                        client,
                        GetFeatureChannelListRequest(feature),
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
