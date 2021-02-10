/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.notifications.channel

/**
 * Interface for channel provider for specific recipient depending on its type.
 */
internal interface ChannelProvider {
    /**
     * gets notification channel for specific recipient depending on its type (prefix).
     * @param recipient recipient address to send notification to. prefix with channel type e.g. "mailto:email@address.com"
     * @return Notification channel for sending notification for given recipient (depending on its type)
     */
    fun getNotificationChannel(recipient: String): NotificationChannel
}
