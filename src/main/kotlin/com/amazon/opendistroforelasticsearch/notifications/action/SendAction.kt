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

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import com.amazon.opendistroforelasticsearch.notifications.channel.ChannelFactory
import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.core.NotificationMessage
import com.amazon.opendistroforelasticsearch.notifications.throttle.Accountant
import com.amazon.opendistroforelasticsearch.notifications.throttle.Counters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.BytesRestResponse
import org.elasticsearch.rest.RestChannel
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestStatus

/**
 * Send action for send notification request.
 */
internal class SendAction(
    private val request: RestRequest,
    private val client: NodeClient,
    private val restChannel: RestChannel
) {
    private val log = LogManager.getLogger(javaClass)

    /**
     * Send notification for the given [request] on the provided [restChannel].
     */
    fun send() {
        log.debug("$PLUGIN_NAME:send")
        val message = RestRequestParser.parse(request)
        val response = restChannel.newBuilder(XContentType.JSON, false).startObject()
            .field("refTag", message.refTag)
        var restStatus = RestStatus.OK // Default to success
        runBlocking {
            val isMessageQuotaAvailableDeferred = async(Dispatchers.IO) { isMessageQuotaAvailable(message) }
            val isMessageQuotaAvailable = isMessageQuotaAvailableDeferred.await()
            if (isMessageQuotaAvailable) {
                response.startArray("recipients")
                val counters = Counters()
                counters.requestCount.incrementAndGet()
                // Fire all the email sending in parallel
                val statusDeferredList = message.recipients.map {
                    async(Dispatchers.IO) { sendMessageToChannel(it, message, counters) }
                }
                val statusList = statusDeferredList.awaitAll()
                // After all operation are executed, update the counters
                launch(Dispatchers.IO) { Accountant.incrementCounters(counters) }
                // Get all the response in sequence
                statusList.forEach {
                    val statusCode = it.second.statusCode
                    val statusText = it.second.statusText
                    if (statusCode != RestStatus.OK) {
                        // if any of the value != success then return corresponding status or 207
                        if (statusCode != restStatus) {
                            restStatus = RestStatus.MULTI_STATUS
                        }
                    }
                    response.startObject()
                        .field("recipient", it.first)
                        .field("statusCode", statusCode.status)
                        .field("statusText", statusText)
                        .endObject()
                    log.info("$PLUGIN_NAME:${message.refTag}:statusCode=$statusCode, statusText=$statusText")
                }
                response.endArray()
            } else {
                restStatus = RestStatus.TOO_MANY_REQUESTS
                val statusText = "Message Sending quota not available"
                response.field("statusCode", restStatus)
                    .field("statusText", statusText)
                log.info("$PLUGIN_NAME:${message.refTag}:statusCode=$restStatus, statusText=$statusText")
            }
        }
        response.endObject()
        restChannel.sendResponse(BytesRestResponse(restStatus, response))
    }

    private fun sendMessageToChannel(recipient: String, message: NotificationMessage, counters: Counters): Pair<String, ChannelMessageResponse> {
        val channel = ChannelFactory.getNotificationChannel(recipient)
        val status = channel.sendMessage(message.refTag, recipient, message.channelMessage, counters)
        return Pair(recipient, status)
    }

    private fun isMessageQuotaAvailable(message: NotificationMessage): Boolean {
        val counters = Counters()
        message.recipients.forEach {
            ChannelFactory.getNotificationChannel(it).updateCounter(message.refTag, it, message.channelMessage, counters)
        }
        return Accountant.isMessageQuotaAvailable(counters)
    }
}
