/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.commons.notifications.NotificationConstants.CHANNEL_MESSAGE_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_SOURCE_TAG
import org.opensearch.commons.utils.contentParserNextToken
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.action.SendChannelMessageAction
import org.opensearch.notifications.model.SendChannelMessageRequest
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.transport.client.node.NodeClient

/**
 * Rest handler for sending a caller-supplied message to one existing notification channel.
 */
internal class SendNotificationRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/feature/send"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return "notifications_send"
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Send a message to a notification channel
             * Request URL: POST [REQUEST_URL/CONFIG_ID_TAG]
             * Request body: [EVENT_SOURCE_TAG] and [CHANNEL_MESSAGE_TAG]
             * Response body: [org.opensearch.commons.notifications.action.SendNotificationResponse]
             * Transport action: [SendChannelMessageAction]
             */
            Route(POST, "$REQUEST_URL/{$CONFIG_ID_TAG}")
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
        val configId = request.param(CONFIG_ID_TAG)
        require(!configId.isNullOrBlank()) { "$CONFIG_ID_TAG is required" }
        val sendRequest = SendChannelMessageRequest.parse(request.contentParserNextToken(), configId)
        return RestChannelConsumer {
            client.execute(
                SendChannelMessageAction.ACTION_TYPE,
                sendRequest,
                RestResponseToXContentListener(it)
            )
        }
    }
}
