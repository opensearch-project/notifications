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

package org.opensearch.notifications.spi

import org.opensearch.notifications.spi.channel.ChannelFactory
import org.opensearch.notifications.spi.message.BaseMessage
import org.opensearch.notifications.spi.model.ChannelMessageResponse
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * This is a client facing Notification class to send the messages
 * to the Notification channels like chime, slack, webhooks, email etc
 */
class Notification private constructor() {

    companion object {
        /**
         * Send the notification message to the corresponding notification
         * channel
         *
         * @param message
         * @return ChannelMessageResponse
         */
        fun sendMessage(message: BaseMessage): ChannelMessageResponse {
            val channel = ChannelFactory.getNotificationChannel(message.configType)
            return AccessController.doPrivileged(
                PrivilegedAction {
                    channel.sendMessage(message)
                } as PrivilegedAction<ChannelMessageResponse>?
            )
        }
    }
}
