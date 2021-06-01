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
package org.opensearch.commons.notifications.action

import org.opensearch.action.ActionType

/**
 * All the transport action information for the Notification plugin
 */
object NotificationsActions {
    /**
     * Create notification configuration transport action name.
     */
    const val CREATE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opensearch/notifications/configs/create"

    /**
     * Update notification configuration transport action name.
     */
    const val UPDATE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opensearch/notifications/configs/update"

    /**
     * Delete notification configuration transport action name.
     */
    const val DELETE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opensearch/notifications/configs/delete"

    /**
     * Get notification configuration transport action name.
     */
    const val GET_NOTIFICATION_CONFIG_NAME = "cluster:admin/opensearch/notifications/configs/get"

    /**
     * Get notification events transport action name.
     */
    const val GET_NOTIFICATION_EVENT_NAME = "cluster:admin/opensearch/notifications/events/get"

    /**
     * Get notification plugin features transport action name.
     */
    const val GET_PLUGIN_FEATURES_NAME = "cluster:admin/opensearch/notifications/features"

    /**
     * Get Config List for feature. Internal only - Inter plugin communication.
     */
    const val GET_FEATURE_CHANNEL_LIST_NAME = "cluster:admin/opensearch/notifications/feature/channels/get"

    /**
     * Send notification message. Internal only - Inter plugin communication.
     */
    const val SEND_NOTIFICATION_NAME = "cluster:admin/opensearch/notifications/feature/send"

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
     * Get notification events transport action type.
     */
    val GET_NOTIFICATION_EVENT_ACTION_TYPE =
        ActionType(GET_NOTIFICATION_EVENT_NAME, ::GetNotificationEventResponse)

    /**
     * Get notification plugin features transport action type.
     */
    val GET_PLUGIN_FEATURES_ACTION_TYPE =
        ActionType(GET_PLUGIN_FEATURES_NAME, ::GetPluginFeaturesResponse)

    /**
     * Get Config List for feature transport action type.
     */
    val GET_FEATURE_CHANNEL_LIST_ACTION_TYPE =
        ActionType(GET_FEATURE_CHANNEL_LIST_NAME, ::GetFeatureChannelListResponse)

    /**
     * Send notification transport action type. Internal only - Inter plugin communication.
     */
    val SEND_NOTIFICATION_ACTION_TYPE =
        ActionType(SEND_NOTIFICATION_NAME, ::SendNotificationResponse)
}
