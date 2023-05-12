/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import org.opensearch.notifications.metrics.Metrics
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
             *     config_id_list=id1,id2,id3 (Other query_params ignored if this is not empty)
             *     from_index=20
             *     max_items=10
             *     sort_order=asc
             *     sort_field=config_type
             *     last_updated_time_ms=from_time..to_time (Range filter field)
             *     created_time_ms=from_time..to_time (Range filter field)
             *     is_enabled=true (Boolean filter field)
             *     config_type=slack,chime (Keyword filter field)
             *     name=test (Text filter field)
             *     description=sample (Text filter field)
             *     email.email_account_id=abc,xyz (Keyword filter field)
             *     email.email_group_id_list=abc,xyz (Keyword filter field)
             *     email.recipient_list.recipient=abc,xyz (Text filter field)
             *     email_group.recipient_list.recipient=abc,xyz (Text filter field)
             *     slack.url=domain (Text filter field)
             *     chime.url=domain (Text filter field)
             *     webhook.url=domain (Text filter field)
             *     smtp_account.host=domain (Text filter field)
             *     smtp_account.from_address=abc,xyz (Text filter field)
             *     smtp_account.method=ssl (Keyword filter field)
             *     sns.topic_arn=abc,xyz (Text filter field)
             *     sns.role_arn=abc,xyz (Text filter field)
             *     ses_account.region=abc,xyz (Text filter field)
             *     ses_account.role_arn=abc,xyz (Text filter field)
             *     ses_account.from_address=abc,xyz (Text filter field)
             *     query=search all above keyword and text filter fields
             *     text_query=search text filter fields from above list
             *     microsoft_teams.url=domain (Text filter field)
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
        Metrics.NOTIFICATIONS_CONFIG_UPDATE_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_CONFIG_UPDATE_INTERVAL_COUNT.counter.increment()
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
        Metrics.NOTIFICATIONS_CONFIG_CREATE_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_CONFIG_CREATE_INTERVAL_COUNT.counter.increment()
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
        Metrics.NOTIFICATIONS_CONFIG_INFO_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_CONFIG_INFO_INTERVAL_COUNT.counter.increment()
        val configId: String? = request.param(CONFIG_ID_TAG)
        val configIdList: String? = request.param(CONFIG_ID_LIST_TAG)
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
        val configRequest = GetNotificationConfigRequest(
            getConfigIdSet(configId, configIdList),
            fromIndex,
            maxItems,
            sortField,
            sortOrder,
            filterParams
        )
        return RestChannelConsumer {
            NotificationsPluginInterface.getNotificationConfig(
                client,
                configRequest,
                RestToXContentListener(it)
            )
        }
    }

    private fun getConfigIdSet(configId: String?, configIdList: String?): Set<String> {
        var retIds: Set<String> = setOf()
        if (configId != null) {
            retIds = setOf(configId)
        }
        if (configIdList != null) {
            retIds = configIdList.split(",").union(retIds)
        }
        return retIds
    }

    private fun executeDeleteRequest(
        request: RestRequest,
        client: NodeClient
    ): RestChannelConsumer {
        Metrics.NOTIFICATIONS_CONFIG_DELETE_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_CONFIG_DELETE_INTERVAL_COUNT.counter.increment()
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
