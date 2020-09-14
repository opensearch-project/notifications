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

package com.amazon.opendistroforelasticsearch.notification.action

import com.amazon.opendistroforelasticsearch.notification.NotificationPlugin.Companion.PLUGIN_NAME
import com.amazon.opendistroforelasticsearch.notification.core.RestRequestParser
import org.apache.logging.log4j.LogManager
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.BytesRestResponse
import org.elasticsearch.rest.RestChannel
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestStatus

class SendAction(private val request: RestRequest, private val client: NodeClient, private val restChannel: RestChannel) {
    private val log = LogManager.getLogger(javaClass)
    fun send() {
        log.info("$PLUGIN_NAME:send")
        val message = RestRequestParser.parse(request)
        val response = restChannel.newBuilder(XContentType.JSON, false).startObject()
            .field("type", "notification_response")
            .startObject("params")
            .field("refTag", message.refTag)
            .startArray("recipients")
        message.recipients.forEach {
            response.startObject()
                .field("recipient", it)
                .field("status", "Success")
                .endObject()
        }
        response.endArray()
            .endObject()
            .endObject()
        restChannel.sendResponse(BytesRestResponse(RestStatus.OK, response))
    }
}
