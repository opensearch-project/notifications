/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package com.amazon.opendistroforelasticsearch.notifications.resthandler

import com.amazon.opendistroforelasticsearch.commons.notifications.NotificationsPluginInterface
import com.amazon.opendistroforelasticsearch.commons.notifications.action.CreateNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.commons.notifications.action.DeleteNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest.Companion.DEFAULT_MAX_ITEMS
import com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest.Companion.FROM_INDEX_TAG
import com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest.Companion.MAX_ITEMS_TAG
import com.amazon.opendistroforelasticsearch.commons.notifications.action.UpdateNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import com.amazon.opendistroforelasticsearch.notifications.util.contentParserNextToken
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.rest.BaseRestHandler.RestChannelConsumer
import org.elasticsearch.rest.BytesRestResponse
import org.elasticsearch.rest.RestHandler.Route
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestRequest.Method.DELETE
import org.elasticsearch.rest.RestRequest.Method.GET
import org.elasticsearch.rest.RestRequest.Method.POST
import org.elasticsearch.rest.RestRequest.Method.PUT
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.rest.action.RestToXContentListener

/**
 * Rest handler for notification configurations.
 */
internal class NotificationConfigRestHandler : PluginBaseHandler() {
    companion object {
        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/configs"
        private const val CONFIG_ID_FIELD = "configId"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return "notifications_config"
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Create a new notification config
             * Request URL: POST [REQUEST_URL]
             * Request body: Ref [com.amazon.opendistroforelasticsearch.commons.notifications.action.CreateNotificationConfigRequest]
             * Response body: [com.amazon.opendistroforelasticsearch.commons.notifications.action.CreateNotificationConfigResponse]
             */
            Route(POST, REQUEST_URL),
            /**
             * Update a notification config
             * Request URL: PUT [REQUEST_URL/{configId}]
             * Request body: Ref [com.amazon.opendistroforelasticsearch.commons.notifications.action.UpdateNotificationConfigRequest]
             * Response body: [com.amazon.opendistroforelasticsearch.commons.notifications.action.UpdateNotificationConfigResponse]
             */
            Route(PUT, "$REQUEST_URL/{$CONFIG_ID_FIELD}"),
            /**
             * Delete a notification config
             * Request URL: DELETE [REQUEST_URL/{configId}]
             * Request body: Ref [com.amazon.opendistroforelasticsearch.commons.notifications.action.DeleteNotificationConfigRequest]
             * Response body: [com.amazon.opendistroforelasticsearch.commons.notifications.action.DeleteNotificationConfigResponse]
             */
            Route(DELETE, "$REQUEST_URL/{$CONFIG_ID_FIELD}"),
            /**
             * Get a notification config
             * Request URL: GET [REQUEST_URL/{configId}]
             * Request body: Ref [com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest]
             * Response body: [com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigResponse]
             */
            Route(GET, "$REQUEST_URL/{$CONFIG_ID_FIELD}"),
            /**
             * Get list of notification configs
             * Request URL: GET [REQUEST_URL]
             * Request body: Ref [com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigRequest]
             * Response body: [com.amazon.opendistroforelasticsearch.commons.notifications.action.GetNotificationConfigResponse]
             */
            Route(GET, REQUEST_URL)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(CONFIG_ID_FIELD, FROM_INDEX_TAG, MAX_ITEMS_TAG)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> RestChannelConsumer {
                NotificationsPluginInterface.createNotificationConfig(
                    client,
                    CreateNotificationConfigRequest.parse(request.contentParserNextToken()),
                    RestToXContentListener(it)
                )
            }
            PUT -> RestChannelConsumer {
                NotificationsPluginInterface.updateNotificationConfig(
                    client,
                    UpdateNotificationConfigRequest.parse(
                        request.contentParserNextToken(),
                        request.param(CONFIG_ID_FIELD)
                    ),
                    RestToXContentListener(it)
                )
            }
            DELETE -> RestChannelConsumer {
                NotificationsPluginInterface.deleteNotificationConfig(
                    client,
                    DeleteNotificationConfigRequest(request.param(CONFIG_ID_FIELD)),
                    RestToXContentListener(it)
                )
            }
            GET -> {
                val configId: String? = request.param(CONFIG_ID_FIELD)
                val fromIndex = request.param(FROM_INDEX_TAG)?.toIntOrNull() ?: 0
                val maxItems = request.param(MAX_ITEMS_TAG)?.toIntOrNull() ?: DEFAULT_MAX_ITEMS
                RestChannelConsumer {
                    NotificationsPluginInterface.getNotificationConfig(
                        client,
                        GetNotificationConfigRequest(fromIndex, maxItems, configId),
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
