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
import org.elasticsearch.rest.RestStatus

/**
 * Empty implementation of the notification channel which responds with error for all requests without any operations.
 */
internal object EmptyChannel : NotificationChannel {
    /**
     * {@inheritDoc}
     */
    override fun sendMessage(
        refTag: String,
        recipient: String,
        title: String,
        channelMessage: ChannelMessage,
        counter: Counters
    ): ChannelMessageResponse {
        return ChannelMessageResponse(recipient,
            RestStatus.UNPROCESSABLE_ENTITY,
            "No Configured Channel for recipient type:${recipient.substringBefore(':', "empty")}")
    }

    /**
     * {@inheritDoc}
     */
    override fun updateCounter(refTag: String, recipient: String, channelMessage: ChannelMessage, counter: Counters) {
        return
    }
}
