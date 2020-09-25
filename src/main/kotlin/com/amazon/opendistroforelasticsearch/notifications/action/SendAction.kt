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
import com.amazon.opendistroforelasticsearch.notifications.core.RestRequestParser
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
class SendAction(
    private val request: RestRequest,
    private val client: NodeClient,
    private val restChannel: RestChannel
) {
    private val log = LogManager.getLogger(javaClass)

    /**
     * Send notification for the given [request] on the provided [restChannel].
     */
    fun send() {
        log.info("$PLUGIN_NAME:send")
        val message = RestRequestParser.parse(request)
        val response = restChannel.newBuilder(XContentType.JSON, false).startObject()
            .field("refTag", message.refTag)
            .startArray("recipients")
        var restStatus = RestStatus.OK // Default to success
        message.recipients.forEach {
            val channel = ChannelFactory.getNotificationChannel(it)
            val status = channel.sendMessage(message.refTag, it, message.channelMessage)
            if (status.statusCode != RestStatus.OK) {
                restStatus = RestStatus.MULTI_STATUS // if any of the value != success then return 207
            }
            response.startObject()
                .field("recipient", it)
                .field("statusCode", status.statusCode.status)
                .field("statusText", status.statusText)
                .endObject()
        }
        response.endArray()
            .endObject()
        restChannel.sendResponse(BytesRestResponse(restStatus, response))
    }
}
