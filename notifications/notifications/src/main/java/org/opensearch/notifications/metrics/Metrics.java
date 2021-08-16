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

package org.opensearch.notifications.metrics;

import com.github.wnameless.json.unflattener.JsonUnflattener;
import org.json.JSONObject;

/**
 * Enum to hold all the metrics that need to be logged into _plugins/_notifications/local/stats API
 */
public enum Metrics {
    REQUEST_TOTAL("request_total", new BasicCounter()),
    REQUEST_INTERVAL_COUNT("request_count", new RollingCounter()),
    REQUEST_SUCCESS("success_count", new RollingCounter()),
    REQUEST_USER_ERROR("failed_request_count_user_error", new RollingCounter()),
    REQUEST_SYSTEM_ERROR("failed_request_count_system_error", new RollingCounter()),

    /**
     * Exceptions from:
     * @see org.opensearch.notifications.action.PluginBaseAction
     */
    NOTIFICATIONS_EXCEPTIONS_OS_STATUS_EXCEPTION("exception.os_status", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_OS_SECURITY_EXCEPTION("exception.os_security", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_VERSION_CONFLICT_ENGINE_EXCEPTION(
            "exception.version_conflict_engine", new RollingCounter()
    ),
    NOTIFICATIONS_EXCEPTIONS_INDEX_NOT_FOUND_EXCEPTION("exception.index_not_found", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_INVALID_INDEX_NAME_EXCEPTION("exception.invalid_index_name", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_ILLEGAL_ARGUMENT_EXCEPTION("exception.illegal_argument", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_ILLEGAL_STATE_EXCEPTION("exception.illegal_state", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_IO_EXCEPTION("exception.io", new RollingCounter()),
    NOTIFICATIONS_EXCEPTIONS_INTERNAL_SERVER_ERROR("exception.internal_server_error", new RollingCounter()),

    // ==== Per REST endpoint metrics ==== //

    // Config Endpoints


    // POST _plugins/_notifications/configs, Create a new notification config
    NOTIFICATIONS_CONFIG_CREATE_TOTAL("notifications_config.create.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_CREATE_INTERVAL_COUNT("notifications_config.create.count", new RollingCounter()),
    NOTIFICATIONS_CONFIG_CREATE_SYSTEM_ERROR("notifications_config.create.system_error", new RollingCounter()),


    // PUT _plugins/_notifications/configs/{configId}, Update a notification config
    NOTIFICATIONS_CONFIG_UPDATE_TOTAL("notifications_config.update.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_UPDATE_INTERVAL_COUNT("notifications_config.update.count", new RollingCounter()),
    NOTIFICATIONS_CONFIG_UPDATE_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_config.update.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_UPDATE_SYSTEM_ERROR("notifications_config.update.system_error", new RollingCounter()),


    // Notification config general user error
    NOTIFICATIONS_CONFIG_USER_ERROR_INVALID_EMAIL_ACCOUNT_ID(
      "notifications_config.user_error.invalid_email_account_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_USER_ERROR_INVALID_EMAIL_GROUP_ID(
            "notifications_config.user_error.invalid_email_group_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_USER_ERROR_NEITHER_EMAIL_NOR_GROUP(
            "notifications_config.user_error.neither_email_nor_group", new RollingCounter()
    ),


    // DELETE _plugins/_notifications/configs/{configId}, Delete a notification config
    NOTIFICATIONS_CONFIG_DELETE_TOTAL("notifications_config.delete.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_DELETE_INTERVAL_COUNT("notifications_config.delete.count", new RollingCounter()),
    NOTIFICATIONS_CONFIG_DELETE_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_config.delete.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_DELETE_SYSTEM_ERROR("notifications_config.delete.system_error", new RollingCounter()),


    // DELETE _plugins/_notifications/configs?{config_id_list}, Delete multiple notification configs
    NOTIFICATIONS_CONFIG_DELETE_LIST_TOTAL("notifications_config.delete_list.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_DELETE_LIST_INTERVAL_COUNT("notifications_config.delete_list.count", new RollingCounter()),
    NOTIFICATIONS_CONFIG_DELETE_LIST_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_config.delete_list.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_DELETE_LIST_SYSTEM_ERROR(
            "notifications_config.delete_list.system_error", new RollingCounter()
    ),


    // GET _plugins/_notifications/configs/{configId}
    NOTIFICATIONS_CONFIG_INFO_TOTAL("notifications_config.info.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_INFO_INTERVAL_COUNT("notifications_config.info.count", new RollingCounter()),
    // add specific user errors for config GET operations
    NOTIFICATIONS_CONFIG_INFO_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_config.info.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_INFO_SYSTEM_ERROR("notifications_config.info.system_error", new RollingCounter()),


    // GET _plugins/_notifications/configs, Get list of notification configs
    NOTIFICATIONS_CONFIG_LIST_TOTAL("notifications_config.list.total", new BasicCounter()),
    NOTIFICATIONS_CONFIG_LIST_INTERVAL_COUNT("notifications_config.list.count", new RollingCounter()),
    NOTIFICATIONS_CONFIG_LIST_USER_ERROR_INVALID_FROM_INDEX(
            "notifications_config.list.user_error.invalid_from_index", new RollingCounter()
    ),
    NOTIFICATIONS_CONFIG_LIST_SYSTEM_ERROR("notifications_config.list.system_error", new RollingCounter()),


    // Event Endpoints


    // GET _plugins/_notifications/configs/{configId}
    NOTIFICATIONS_EVENTS_INFO_TOTAL("notifications_events.info.total", new BasicCounter()),
    NOTIFICATIONS_EVENTS_INFO_INTERVAL_COUNT("notifications_events.info.count", new RollingCounter()),
    NOTIFICATIONS_EVENTS_INFO_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_events.info.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_EVENTS_INFO_SYSTEM_ERROR(
            "notifications_events.info.system_error", new RollingCounter()
    ),


    // GET _plugins/_notifications/configs
    NOTIFICATIONS_EVENTS_LIST_TOTAL("notifications_events.list.total", new BasicCounter()),
    NOTIFICATIONS_EVENTS_LIST_INTERVAL_COUNT("notifications_events.list.count", new RollingCounter()),
    NOTIFICATIONS_EVENTS_LIST_USER_ERROR_INVALID_CONFIG_ID(
            "notifications_events.list.user_error.invalid_config_id", new RollingCounter()
    ),
    NOTIFICATIONS_EVENTS_LIST_USER_ERROR_INVALID_FROM_INDEX(
            "notifications_events.list.user_error.invalid_from_index", new RollingCounter()
    ),
    NOTIFICATIONS_EVENTS_LIST_SYSTEM_ERROR(
            "notifications_events.list.system_error", new RollingCounter()
    ),


    // Feature Channels Endpoints

    // GET _plugins/_notifications/feature/channels/{featureTag}
    NOTIFICATIONS_FEATURE_CHANNELS_INFO_TOTAL("notifications_feature_channels.info.total", new BasicCounter()),
    NOTIFICATIONS_FEATURE_CHANNELS_INFO_INTERVAL_COUNT(
            "notifications_feature_channels.info.count", new RollingCounter()
    ),
    NOTIFICATIONS_FEATURE_CHANNELS_INFO_USER_ERROR_INVALID_FEATURE_TAG(
            "notifications_feature_channels.info.user_error.invalid_feature_tag", new RollingCounter()
    ),
    NOTIFICATIONS_FEATURE_CHANNELS_INFO_USER_ERROR_INVALID_FROM_INDEX(
            "notifications_feature_channels.info.user_error.invalid_from_index", new RollingCounter()
    ),
    NOTIFICATIONS_FEATURE_CHANNELS_INFO_SYSTEM_ERROR(
            "notifications_feature_channels.info.system_error", new RollingCounter()
    ),


    // Features Endpoints

    // GET _plugins/_notifications/features
    NOTIFICATIONS_FEATURES_INFO_TOTAL("notifications_features.info.total", new BasicCounter()),
    NOTIFICATIONS_FEATURES_INFO_INTERVAL_COUNT("notifications_features.info.count", new RollingCounter()),
    NOTIFICATIONS_FEATURES_INFO_USER_ERROR_INVALID_FROM_INDEX(
            "notifications_features.info.user_error.invalid_from_index", new RollingCounter()
    ),
    NOTIFICATIONS_FEATURES_INFO_SYSTEM_ERROR("notifications_features.info.system_error", new RollingCounter()),


    // Send Message Endpoints

    // POST _plugins/_notifications/send
    NOTIFICATIONS_SEND_MESSAGE_TOTAL("notifications.send_message.total", new BasicCounter()),
    NOTIFICATIONS_SEND_MESSAGE_INTERVAL_COUNT("notifications.send_message.count", new RollingCounter()),
    // user errors for send message?
    NOTIFICATIONS_SEND_MESSAGE_USER_ERROR_NOT_FOUND(
            "notifications.send_message.user_error.not_found", new RollingCounter()
    ),
    NOTIFICATIONS_SEND_MESSAGE_SYSTEM_ERROR("notifications.send_message.system_error", new RollingCounter()),

    // Track message destinations
    NOTIFICATIONS_MESSAGE_DESTINATION_SLACK("notifications.message_destination.slack", new BasicCounter()),
    NOTIFICATIONS_MESSAGE_DESTINATION_CHIME("notifications.message_destination.chime", new BasicCounter()),
    NOTIFICATIONS_MESSAGE_DESTINATION_WEBHOOK("notifications.message_destination.webhook", new BasicCounter()),
    NOTIFICATIONS_MESSAGE_DESTINATION_EMAIL("notifications.message_destination.email", new BasicCounter()),
    NOTIFICATIONS_MESSAGE_DESTINATION_SES_ACCOUNT(
            "notifications.message_destination.ses_account", new BasicCounter()
    ), // TODO: add after implementation added
    NOTIFICATIONS_MESSAGE_DESTINATION_SMTP_ACCOUNT(
            "notifications.message_destination.smtp_account", new BasicCounter()
    ), // TODO: add after implementation added
    NOTIFICATIONS_MESSAGE_DESTINATION_EMAIL_GROUP(
            "notifications.message_destination.email_group", new BasicCounter()
    ), // TODO: add after implementation added
    NOTIFICATIONS_MESSAGE_DESTINATION_SNS("notifications.message_destination.sns", new BasicCounter()),


    // Send Test Message Endpoints

    // GET _plugins/_notifications/feature/test/{configId}
    NOTIFICATIONS_SEND_TEST_MESSAGE_TOTAL("notifications.send_test_message.total", new BasicCounter()),
    NOTIFICATIONS_SEND_TEST_MESSAGE_INTERVAL_COUNT(
            "notifications.send_test_message.interval_count", new RollingCounter()
    ),
    // Send test message exceptions are thrown by the Send Message Action

    NOTIFICATIONS_SECURITY_USER_ERROR("security_user_error", new RollingCounter()),
    NOTIFICATIONS_PERMISSION_USER_ERROR("permissions_user_error", new RollingCounter());

    private final String name;
    private final Counter<?> counter;

    Metrics(String name, Counter<?> counter) {
        this.name = name;
        this.counter = counter;
    }

    public String getName() {
        return name;
    }

    public Counter<?> getCounter() {
        return counter;
    }

    private static final Metrics[] values = values();

    /**
     * Converts the enum metric values to JSON string
     */
    public static String collectToJSON() {
        JSONObject metricsJSONObject = new JSONObject();
        for (Metrics metric: values) {
            metricsJSONObject.put(metric.name, metric.counter.getValue());
        }
        return metricsJSONObject.toString();
    }

    /**
     * Unflattens the JSON to nested JSON for easy readability and parsing
     * The metric name is unflattened in the output JSON on the period '.' delimiter
     *
     * For ex:  { "a.b.c_d" : 2 } becomes
     *{
     *   "a" : {
     *     "b" : {
     *       "c_d" : 2
     *     }
     *   }
     * }
     */

    public static String collectToFlattenedJSON() {
        return JsonUnflattener.unflatten(collectToJSON());
    }
}
