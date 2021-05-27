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
package org.opensearch.notifications.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.DEFAULT_MAX_ITEMS
import org.opensearch.commons.notifications.NotificationConstants.FROM_INDEX_TAG
import org.opensearch.commons.notifications.NotificationConstants.MAX_ITEMS_TAG
import org.opensearch.commons.notifications.NotificationConstants.SORT_FIELD_TAG
import org.opensearch.commons.notifications.NotificationConstants.SORT_ORDER_TAG
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.utils.contentParserNextToken
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.index.ConfigQueryHelper
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.DELETE
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestRequest.Method.PUT
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener
import org.opensearch.search.sort.SortOrder

/**
 * Rest handler for notification configurations.
 */
internal class NotificationConfigRestHandler : PluginBaseHandler() {
    companion object {
        private val log by logger(NotificationConfigRestHandler::class.java)

        /**
         * Base URL for this handler
         */
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/configs"
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
             * Request body: Ref [org.opensearch.commons.notifications.action.CreateNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.CreateNotificationConfigResponse]
             */
            Route(POST, REQUEST_URL),
            /**
             * Update a notification config
             * Request URL: PUT [REQUEST_URL/{configId}]
             * Request body: Ref [org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse]
             */
            Route(PUT, "$REQUEST_URL/{$CONFIG_ID_TAG}"),
            /**
             * Delete a notification config
             * Request URL: DELETE [REQUEST_URL/{configId}]
             * Request body: Ref [org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse]
             */
            Route(DELETE, "$REQUEST_URL/{$CONFIG_ID_TAG}"),
            /**
             * Delete a notification config
             * Request URL: DELETE [REQUEST_URL?config_id=id] or [REQUEST_URL?config_id_list=comma_separated_ids]
             * Request body: Ref [org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse]
             */
            Route(DELETE, REQUEST_URL),
            /**
             * Get a notification config
             * Request URL: GET [REQUEST_URL/{configId}]
             * Request body: Ref [org.opensearch.commons.notifications.action.GetNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.GetNotificationConfigResponse]
             */
            Route(GET, "$REQUEST_URL/{$CONFIG_ID_TAG}"),
            /**
             * Get list of notification configs
             * Request URL: GET [REQUEST_URL?config_id=id] or [REQUEST_URL?<query_params>]
             * <query_params> ->
             *     from_index=20
             *     max_items=10
             *     sort_order=asc
             *     sort_field=config_type
             *     last_updated_time_ms=from_time..to_time
             *     created_time_ms=from_time..to_time
             *     is_enabled=true
             *     config_type=slack,chime
             *     feature_list=alerting,reports
             *     name=test
             *     description=sample
             *     email.email_account_id=abc,xyz
             *     email.email_group_id_list=abc,xyz
             *     smtp_account.method=ssl
             *     slack.url=domain
             *     chime.url=domain
             *     webhook.url=domain
             *     email.recipient_list=abc,xyz
             *     email_group.recipient_list=abc,xyz
             *     smtp_account.host=domain
             *     smtp_account.from_address=abc,xyz
             *     smtp_account.recipient_list=abc,xyz
             *     query=search all above fields
             * Request body: Ref [org.opensearch.commons.notifications.action.GetNotificationConfigRequest]
             * Response body: [org.opensearch.commons.notifications.action.GetNotificationConfigResponse]
             */
            Route(GET, REQUEST_URL)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(
            CONFIG_ID_TAG,
            CONFIG_ID_LIST_TAG,
            SORT_FIELD_TAG,
            SORT_ORDER_TAG,
            FROM_INDEX_TAG,
            MAX_ITEMS_TAG
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> executePostRequest(request, client)
            PUT -> executePutRequest(request, client)
            DELETE -> executeDeleteRequest(request, client)
            GET -> executeGetRequest(request, client)

            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }

    private fun executePutRequest(
        request: RestRequest,
        client: NodeClient
    ) = RestChannelConsumer {
        NotificationsPluginInterface.updateNotificationConfig(
            client,
            UpdateNotificationConfigRequest.parse(
                request.contentParserNextToken(),
                request.param(CONFIG_ID_TAG)
            ),
            RestToXContentListener(it)
        )
    }

    private fun executePostRequest(
        request: RestRequest,
        client: NodeClient
    ) = RestChannelConsumer {
        NotificationsPluginInterface.createNotificationConfig(
            client,
            CreateNotificationConfigRequest.parse(request.contentParserNextToken()),
            RestToXContentListener(it)
        )
    }

    private fun executeGetRequest(
        request: RestRequest,
        client: NodeClient
    ): RestChannelConsumer {
        val configId: String? = request.param(CONFIG_ID_TAG)
        val sortField: String? = request.param(SORT_FIELD_TAG)
        val sortOrderString: String? = request.param(SORT_ORDER_TAG)
        val sortOrder: SortOrder? = if (sortOrderString == null) {
            null
        } else {
            SortOrder.fromString(sortOrderString)
        }
        val fromIndex = request.param(FROM_INDEX_TAG)?.toIntOrNull() ?: 0
        val maxItems = request.param(MAX_ITEMS_TAG)?.toIntOrNull() ?: DEFAULT_MAX_ITEMS
        val filterParams = request.params()
            .filter { ConfigQueryHelper.FILTER_PARAMS.contains(it.key) }
            .map { Pair(it.key, request.param(it.key)) }
            .toMap()
        log.info(
            "$LOG_PREFIX:executeGetRequest from:$fromIndex, maxItems:$maxItems," +
                " sortField:$sortField, sortOrder=$sortOrder, filters=$filterParams"
        )
        return RestChannelConsumer {
            NotificationsPluginInterface.getNotificationConfig(
                client,
                GetNotificationConfigRequest(configId, fromIndex, maxItems, sortField, sortOrder, filterParams),
                RestToXContentListener(it)
            )
        }
    }

    private fun executeDeleteRequest(
        request: RestRequest,
        client: NodeClient
    ): RestChannelConsumer {
        val configId: String? = request.param(CONFIG_ID_TAG)
        val configIdSet: Set<String> =
            request.paramAsStringArray(CONFIG_ID_LIST_TAG, arrayOf(configId))
                .filter { s -> !s.isNullOrBlank() }
                .toSet()
        return RestChannelConsumer {
            if (configIdSet.isEmpty()) {
                it.sendResponse(
                    BytesRestResponse(
                        RestStatus.BAD_REQUEST,
                        "either $CONFIG_ID_TAG or $CONFIG_ID_LIST_TAG is required"
                    )
                )
            } else {
                NotificationsPluginInterface.deleteNotificationConfig(
                    client,
                    DeleteNotificationConfigRequest(configIdSet),
                    RestResponseToXContentListener(it)
                )
            }
        }
    }
}
