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

package org.opensearch.notifications.spi.channel.webhook

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.opensearch.notifications.spi.channel.NotificationChannel
import org.opensearch.notifications.spi.channel.client.ChannelHttpClient
import org.opensearch.notifications.spi.channel.client.ChannelHttpClientPool
import org.opensearch.notifications.spi.message.WebhookMessage
import org.opensearch.notifications.spi.model.ChannelMessageResponse
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * Notification channel for sending mail to Email server.
 */
internal class WebhookChannel : NotificationChannel<WebhookMessage, ChannelHttpClient> {

    private val logger: Logger = LogManager.getLogger(NotificationChannel::class.java)
    var channelHttpClient: ChannelHttpClient = ChannelHttpClientPool.httpClient

    override fun sendMessage(message: WebhookMessage): ChannelMessageResponse {
        return try {
            val response = getClient(message).execute(message)
            ChannelMessageResponse(
                recipient = message.configType.name,
                statusCode = RestStatus.OK,
                statusText = response
            )
        } catch (exception: IOException) {
            logger.error("Exception publishing Message: $message", exception)
            ChannelMessageResponse(
                recipient = message.configType.name,
                statusCode = RestStatus.INTERNAL_SERVER_ERROR,
                statusText = "Failed to send message"
            )
        }
    }

    override fun getClient(message: WebhookMessage): ChannelHttpClient {
        return channelHttpClient
    }
}
