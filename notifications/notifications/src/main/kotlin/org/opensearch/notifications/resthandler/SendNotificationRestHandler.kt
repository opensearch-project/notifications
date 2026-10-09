/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.commons.notifications.NotificationConstants.CHANNEL_MESSAGE_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_SOURCE_TAG
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.utils.contentParserNextToken
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.transport.client.node.NodeClient

/**
 * Rest handler for sending a message to an existing notification channel.
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
        val sendNotificationRequest = parse(request.contentParserNextToken(), configId)
        return RestChannelConsumer {
            client.execute(
                NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE,
                sendNotificationRequest,
                RestResponseToXContentListener(it)
            )
        }
    }

    /**
     * The channel is taken from the path and the user from the authenticated caller, so the body carries
     * only the event source and the message.
     */
    private fun parse(parser: XContentParser, configId: String): SendNotificationRequest {
        var eventSource: EventSource? = null
        var channelMessage: ChannelMessage? = null
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                EVENT_SOURCE_TAG -> eventSource = EventSource.parse(parser)
                CHANNEL_MESSAGE_TAG -> channelMessage = ChannelMessage.parse(parser)
                else -> throw IllegalArgumentException("Unexpected field $fieldName")
            }
        }
        requireNotNull(eventSource) { "$EVENT_SOURCE_TAG is required" }
        requireNotNull(channelMessage) { "$CHANNEL_MESSAGE_TAG is required" }
        return SendNotificationRequest(eventSource, channelMessage, listOf(configId), null)
    }
}
