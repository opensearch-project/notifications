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
package com.amazon.opendistroforelasticsearch.commons.notifications.action

import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.ActionType
import org.elasticsearch.client.node.NodeClient

/**
 * All the transport action information for the Notification plugin
 */
object NotificationsActions {
    /**
     * Create notification configuration transport action name.
     */
    const val CREATE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opendistro/notifications/configs/create"

    /**
     * Update notification configuration transport action name.
     */
    const val UPDATE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opendistro/notifications/configs/update"

    /**
     * Delete notification configuration transport action name.
     */
    const val DELETE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opendistro/notifications/configs/delete"

    /**
     * Get notification configuration transport action name.
     */
    const val GET_NOTIFICATION_CONFIG_NAME = "cluster:admin/opendistro/notifications/configs/get"

    /**
     * Create notification configuration transport action type.
     */
    val CREATE_NOTIFICATION_CONFIG_ACTION_TYPE =
        ActionType(CREATE_NOTIFICATION_CONFIG_NAME, ::CreateNotificationConfigResponse)

    /**
     * Update notification configuration transport action type.
     */
    val UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE =
        ActionType(UPDATE_NOTIFICATION_CONFIG_NAME, ::UpdateNotificationConfigResponse)

    /**
     * Delete notification configuration transport action type.
     */
    val DELETE_NOTIFICATION_CONFIG_ACTION_TYPE =
        ActionType(DELETE_NOTIFICATION_CONFIG_NAME, ::DeleteNotificationConfigResponse)

    /**
     * Get notification configuration transport action type.
     */
    val GET_NOTIFICATION_CONFIG_ACTION_TYPE =
        ActionType(GET_NOTIFICATION_CONFIG_NAME, ::GetNotificationConfigResponse)

    /**
     * Create notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun createNotificationConfig(
        client: NodeClient,
        request: CreateNotificationConfigRequest,
        listener: ActionListener<CreateNotificationConfigResponse>
    ) {
        client.execute(
            CREATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            listener
        )
    }

    /**
     * Update notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun updateNotificationConfig(
        client: NodeClient,
        request: UpdateNotificationConfigRequest,
        listener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        client.execute(
            UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            listener
        )
    }

    /**
     * Delete notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun deleteNotificationConfig(
        client: NodeClient,
        request: DeleteNotificationConfigRequest,
        listener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        client.execute(
            DELETE_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            listener
        )
    }

    /**
     * Get notification configuration.
     * @param client Node client for making transport action
     * @param request The request object
     * @param listener The listener for getting response
     */
    fun getNotificationConfig(
        client: NodeClient,
        request: GetNotificationConfigRequest,
        listener: ActionListener<GetNotificationConfigResponse>
    ) {
        client.execute(
            GET_NOTIFICATION_CONFIG_ACTION_TYPE,
            request,
            listener
        )
    }
}
