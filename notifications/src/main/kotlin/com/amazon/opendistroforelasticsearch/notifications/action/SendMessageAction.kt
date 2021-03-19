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

package com.amazon.opendistroforelasticsearch.notifications.action

import com.amazon.opendistroforelasticsearch.commons.authuser.User
import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notifications.channel.ChannelFactory
import com.amazon.opendistroforelasticsearch.notifications.model.ChannelMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.model.SendMessageRequest
import com.amazon.opendistroforelasticsearch.notifications.model.SendMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.throttle.Accountant
import com.amazon.opendistroforelasticsearch.notifications.throttle.Counters
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.ActionType
import org.elasticsearch.action.support.ActionFilters
import org.elasticsearch.client.Client
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.transport.TransportService

/**
 * Send message action for send notification request.
 */
internal class SendMessageAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<SendMessageRequest, SendMessageResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::SendMessageRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/notifications/send"
        internal val ACTION_TYPE = ActionType(NAME, ::SendMessageResponse)
        private val log by logger(SendMessageAction::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: SendMessageRequest, user: User?): SendMessageResponse {
        log.debug("$LOG_PREFIX:send")
        if (!isMessageQuotaAvailable(request)) {
            log.info("$LOG_PREFIX:${request.refTag}:Message Sending quota not available")
            throw ElasticsearchStatusException("Message Sending quota not available", RestStatus.TOO_MANY_REQUESTS)
        }
        val statusList: List<ChannelMessageResponse> = sendMessagesInParallel(request)
        statusList.forEach {
            log.info("$LOG_PREFIX:${request.refTag}:statusCode=${it.statusCode}, statusText=${it.statusText}")
        }
        return SendMessageResponse(request.refTag, statusList)
    }

    private fun sendMessagesInParallel(sendMessageRequest: SendMessageRequest): List<ChannelMessageResponse> {
        val counters = Counters()
        counters.requestCount.incrementAndGet()
        val statusList: List<ChannelMessageResponse>
        // Fire all the message sending in parallel
        runBlocking {
            val statusDeferredList = sendMessageRequest.recipients.map {
                async(Dispatchers.IO) { sendMessageToChannel(it, sendMessageRequest, counters) }
            }
            statusList = statusDeferredList.awaitAll()
        }
        // After all operation are executed, update the counters
        Accountant.incrementCounters(counters)
        return statusList
    }

    private fun sendMessageToChannel(recipient: String, sendMessageRequest: SendMessageRequest, counters: Counters): ChannelMessageResponse {
        val channel = ChannelFactory.getNotificationChannel(recipient)
        return channel.sendMessage(sendMessageRequest.refTag, recipient, sendMessageRequest.channelMessage, counters)
    }

    private fun isMessageQuotaAvailable(sendMessageRequest: SendMessageRequest): Boolean {
        val counters = Counters()
        sendMessageRequest.recipients.forEach {
            ChannelFactory.getNotificationChannel(it).updateCounter(sendMessageRequest.refTag, it, sendMessageRequest.channelMessage, counters)
        }
        return Accountant.isMessageQuotaAvailable(counters)
    }
}
