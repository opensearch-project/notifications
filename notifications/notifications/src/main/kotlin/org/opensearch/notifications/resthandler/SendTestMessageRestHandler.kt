/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.action.SendTestNotificationAction
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.model.SendTestNotificationRequest
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.transport.client.node.NodeClient

/**
 * Rest handler for getting notification features.
 */
internal class SendTestMessageRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/feature/test"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return "notifications_send_test"
    }

    /**
     * {@inheritDoc}
     */
    override fun replacedRoutes(): List<ReplacedRoute> {
        return listOf(
            /**
             * Send test notification message
             * Request URL: POST [REQUEST_URL/CONFIG_ID_TAG]
             * Request body: Ref [org.opensearch.commons.notifications.action.SendNotificationRequest]
             * Response body: [org.opensearch.commons.notifications.action.SendNotificationResponse]
             */
            // Using GET with this API has been deprecated, it will be removed in favor of the POST equivalent in the next major version.
            ReplacedRoute(POST, "$REQUEST_URL/{$CONFIG_ID_TAG}", GET, "$REQUEST_URL/{$CONFIG_ID_TAG}")
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(CONFIG_ID_TAG)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> executeSendTestMessage(request, client)
            GET -> executeSendTestMessage(request, client)
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }

    private fun executeSendTestMessage(
        request: RestRequest,
        client: NodeClient
    ) = RestChannelConsumer {
        Metrics.NOTIFICATIONS_SEND_TEST_MESSAGE_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_SEND_TEST_MESSAGE_INTERVAL_COUNT.counter.increment()
        val configId = request.param(CONFIG_ID_TAG)
        val sendTestNotificationRequest = SendTestNotificationRequest(configId)
        client.execute(
            SendTestNotificationAction.ACTION_TYPE,
            sendTestNotificationRequest,
            RestResponseToXContentListener(it)
        )
    }
}
