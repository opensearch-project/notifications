/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.notifications.action.SendTestNotificationAction

val ALL_ACCESS_ROLE = "all_access"
val NOTIFICATION_FULL_ACCESS_ROLE = "notifications_full_access"
val NOTIFICATION_READ_ONLY_ACCESS = "notifications_read_access"
val NOTIFICATION_NO_ACCESS_ROLE = "no_access"
val NOTIFICATION_CREATE_CONFIG_ACCESS = "notifications_create_config_access"
val NOTIFICATION_UPDATE_CONFIG_ACCESS = "notifications_update_config_access"
val NOTIFICATION_DELETE_CONFIG_ACCESS = "notifications_delete_config_access"
val NOTIFICATION_GET_CONFIG_ACCESS = "notifications_get_config_access"
val NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS = "notifications_get_plugin_access"
val NOTIFICATION_GET_CHANNEL_ACCESS = "notifications_get_channel_access"
val NOTIFICATION_TEST_SEND_ACCESS = "notifications_test_send_access"

val ROLE_TO_PERMISSION_MAPPING =
    mapOf(
        NOTIFICATION_NO_ACCESS_ROLE to "",
        NOTIFICATION_CREATE_CONFIG_ACCESS to NotificationsActions.CREATE_NOTIFICATION_CONFIG_NAME,
        NOTIFICATION_UPDATE_CONFIG_ACCESS to NotificationsActions.UPDATE_NOTIFICATION_CONFIG_NAME,
        NOTIFICATION_DELETE_CONFIG_ACCESS to NotificationsActions.DELETE_NOTIFICATION_CONFIG_NAME,
        NOTIFICATION_GET_CONFIG_ACCESS to NotificationsActions.GET_NOTIFICATION_CONFIG_NAME,
        NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS to NotificationsActions.GET_PLUGIN_FEATURES_NAME,
        NOTIFICATION_GET_CHANNEL_ACCESS to NotificationsActions.GET_CHANNEL_LIST_NAME,
        NOTIFICATION_TEST_SEND_ACCESS to SendTestNotificationAction.NAME,
    )
