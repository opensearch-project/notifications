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
package com.amazon.opensearch.commons.notifications

import com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_USER_INFO_THREAD_CONTEXT
import com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import com.amazon.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import com.amazon.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import com.amazon.opensearch.commons.notifications.action.GetFeatureChannelListResponse
import com.amazon.opensearch.commons.notifications.action.GetNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.GetNotificationConfigResponse
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.CREATE_NOTIFICATION_CONFIG_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.DELETE_NOTIFICATION_CONFIG_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.GET_FEATURE_CHANNEL_LIST_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.GET_NOTIFICATION_CONFIG_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.NotificationsActions.UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE
import com.amazon.opensearch.commons.notifications.action.SendNotificationRequest
import com.amazon.opensearch.commons.notifications.action.SendNotificationResponse
import com.amazon.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import com.amazon.opensearch.commons.notifications.model.ChannelMessage
import com.amazon.opensearch.commons.notifications.model.Feature
import com.amazon.opensearch.commons.notifications.model.NotificationInfo
import com.amazon.opensearch.commons.utils.SecureClientWrapper
import org.opensearch.action.ActionListener
import org.opensearch.client.node.NodeClient

/**
 * All the transport action plugin interfaces for the Notification plugin
 */
object NotificationsPluginInterface {

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

    /**
     * Get notification channel configuration enabled for a feature.
     * @param client Node client for making transport action
     * @param feature The feature name requested
     * @param listener The listener for getting response
     */
    fun getFeatureChannelList(
        client: NodeClient,
        feature: Feature,
        listener: ActionListener<GetFeatureChannelListResponse>
    ) {
        val threadContext: String? =
            client.threadPool().threadContext.getTransient<String>(OPENDISTRO_SECURITY_USER_INFO_THREAD_CONTEXT)
        val wrapper = SecureClientWrapper(client) // Executing request in privileged mode
        wrapper.execute(
            GET_FEATURE_CHANNEL_LIST_ACTION_TYPE,
            GetFeatureChannelListRequest(feature, threadContext),
            listener
        )
    }

    /**
     * Send notification API enabled for a feature.
     * @param client Node client for making transport action
     * @param notificationInfo The notification information
     * @param channelMessage The notification message
     * @param channelIds The list of channel ids to send message to.
     * @param listener The listener for getting response
     */
    fun sendNotification(
        client: NodeClient,
        notificationInfo: NotificationInfo,
        channelMessage: ChannelMessage,
        channelIds: List<String>,
        listener: ActionListener<SendNotificationResponse>
    ) {
        val threadContext: String? =
            client.threadPool().threadContext.getTransient<String>(OPENDISTRO_SECURITY_USER_INFO_THREAD_CONTEXT)
        val wrapper = SecureClientWrapper(client) // Executing request in privileged mode
        wrapper.execute(
            SEND_NOTIFICATION_ACTION_TYPE,
            SendNotificationRequest(notificationInfo, channelMessage, channelIds, threadContext),
            listener
        )
    }
}
