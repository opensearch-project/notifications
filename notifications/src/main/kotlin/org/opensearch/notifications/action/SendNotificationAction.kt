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

package org.opensearch.notifications.action

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.notifications.spi.Notification
import org.opensearch.notifications.spi.message.BaseMessage
import org.opensearch.notifications.spi.message.CustomWebhookMessage
import org.opensearch.notifications.spi.message.WebhookMessage
import org.opensearch.notifications.spi.model.ChannelMessageResponse
import org.opensearch.notifications.spi.model.DestinationType
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Send Notification transport action
 */
internal class SendNotificationAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<SendNotificationRequest, SendNotificationResponse>(
    NotificationsActions.SEND_NOTIFICATION_NAME,
    transportService,
    client,
    actionFilters,
    ::SendNotificationRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: SendNotificationRequest,
        listener: ActionListener<SendNotificationResponse>
    ) {
        val transformedRequest = request as? SendNotificationRequest
            ?: recreateObject(request) { SendNotificationRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: SendNotificationRequest,
        user: User?
    ): SendNotificationResponse {
        // TODO: create notification audit doc, get the id

        // send message in parallel
        val statusList = sendMessagesInParallel(request)

        // TODO: parse statusList to corresponding status objects
        // TODO save status into notification audit index

        // return a response with notification audit id
        return SendNotificationResponse("TODO-notificationId")
    }

    private fun sendMessagesInParallel(sendNotificationRequest: SendNotificationRequest): List<ChannelMessageResponse> {
        val statusList: List<ChannelMessageResponse>
        // parse info from sendNotificationRequest, and build a message with config type
        val messageList = sendNotificationRequest.channelIds.map {
            val config = ConfigIndexingActions.get(it)
            prepareMessage(it, config, sendNotificationRequest.eventSource.title, sendNotificationRequest.channelMessage)
        }
        // Fire all the message sending in parallel
        runBlocking {
            val statusDeferredList = messageList.map {
                async(Dispatchers.IO) { Notification.sendMessage(it) }
            }
            statusList = statusDeferredList.awaitAll()
        }
        return statusList
    }

    private fun prepareMessage(
        channelId: String,
        config: NotificationConfig,
        title: String,
        channelMessage: ChannelMessage
    ): BaseMessage {
        val configType = config.configType
        val configData = config.configData
        // convert to the the messageContent class that SPI accepts
        val attachment = channelMessage.attachment
        val messageContent = MessageContent(
            channelMessage.textDescription,
            channelMessage.htmlDescription,
            attachment?.fileName,
            attachment?.fileEncoding,
            attachment?.fileData,
            attachment?.fileContentType
        )

        when (configType) {
            ConfigType.CHIME -> {
                configData as Chime
                return WebhookMessage(configData.url, title, DestinationType.Chime, messageContent, channelId)
            }
            ConfigType.SLACK -> {
                configData as Slack
                return WebhookMessage(configData.url, title, DestinationType.Slack, messageContent, channelId)
            }
            ConfigType.WEBHOOK -> {
                configData as Webhook
                return CustomWebhookMessage(
                    configData.url,
                    title,
                    DestinationType.Webhook,
                    messageContent,
                    channelId,
                    configData.headerParams
                )
            } else -> {
                throw IllegalArgumentException("invalid config type")
            }
            // TODO: Add other channels
        }
    }
}
