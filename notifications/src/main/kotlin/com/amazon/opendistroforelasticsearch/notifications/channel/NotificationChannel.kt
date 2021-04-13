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

import com.amazon.opendistroforelasticsearch.commons.notifications.model.ChannelMessage
import com.amazon.opendistroforelasticsearch.notifications.model.ChannelMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.throttle.Counters

/**
 * Interface for sending notification message over a implemented channel.
 */
internal interface NotificationChannel {

    /**
     * Update the counter if the notification message is over this channel. Do not actually send message.
     * Used for checking message quotas.
     *
     * @param refTag ref tag for logging purpose
     * @param recipient recipient address to send notification to
     * @param channelMessage The message to send notification
     * @param counter The counter object to update the detail for accounting purpose
     */
    fun updateCounter(refTag: String, recipient: String, channelMessage: ChannelMessage, counter: Counters)

    /**
     * Sending notification message over this channel.
     *
     * @param refTag ref tag for logging purpose
     * @param recipient recipient address to send notification to
     * @param title The title to send notification
     * @param channelMessage The message to send notification
     * @param counter The counter object to update the detail for accounting purpose
     * @return Channel message response
     */
    fun sendMessage(refTag: String, recipient: String, title: String, channelMessage: ChannelMessage, counter: Counters): ChannelMessageResponse
}
