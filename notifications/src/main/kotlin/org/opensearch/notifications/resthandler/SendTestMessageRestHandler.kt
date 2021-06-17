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
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_TAG
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener

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
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Get notification features
             * Request URL: GET [REQUEST_URL/CONFIG_ID_TAG]
             * Request body: Ref [org.opensearch.commons.notifications.action.SendNotificationRequest]
             * Response body: [org.opensearch.commons.notifications.action.SendNotificationResponse]
             */
            Route(GET, "$REQUEST_URL/{$CONFIG_ID_TAG}")
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(CONFIG_ID_TAG, FEATURE_TAG)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
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
        val feature = Feature.fromTagOrDefault(request.param(FEATURE_TAG, Feature.NONE.tag))
        val configId = request.param(CONFIG_ID_TAG)
        val source = generateEventSource(feature, configId)
        val message = ChannelMessage(
            getMessageTextDescription(feature, configId),
            getMessageHtmlDescription(feature, configId),
            null
        )
        val channelIds = listOf(configId)
        NotificationsPluginInterface.sendNotification(
            client,
            source,
            message,
            channelIds,
            RestToXContentListener(it)
        )
    }

    private fun generateEventSource(feature: Feature, configId: String): EventSource {
        return EventSource(
            getMessageTitle(feature, configId),
            configId,
            feature,
            SeverityType.INFO
        )
    }

    private fun getMessageTitle(feature: Feature, configId: String): String {
        return "[$feature] Test Message-$configId" // TODO: change as spec
    }

    private fun getMessageTextDescription(feature: Feature, configId: String): String {
        return "Test Message for config id $configId from feature ${feature.tag}" // TODO: change as spec
    }

    private fun getMessageHtmlDescription(feature: Feature, configId: String): String {
        return """
            <html>
            <header><title>Test Message</title></header>
            <body>
            <p>Test Message for config id $configId from feature ${feature.tag}</p>
            </body>
            </html>
        """.trimIndent() // TODO: change as spec
    }
}
