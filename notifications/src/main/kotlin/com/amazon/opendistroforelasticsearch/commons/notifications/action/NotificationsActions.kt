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

import org.elasticsearch.action.ActionType

/**
 * All the transport action information for the Notification plugin
 */
object NotificationsActions {
    /**
     * Create notification configuration transport action name.
     */
    const val CREATE_NOTIFICATION_CONFIG_NAME = "cluster:admin/opendistro/notifications/configs/create"

    /**
     * Create notification configuration transport action type.
     */
    val CREATE_NOTIFICATION_CONFIG_ACTION_TYPE =
        ActionType(CREATE_NOTIFICATION_CONFIG_NAME, ::CreateNotificationConfigResponse)
}
