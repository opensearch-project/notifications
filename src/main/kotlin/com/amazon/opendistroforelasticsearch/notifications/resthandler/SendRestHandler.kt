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

package com.amazon.opendistroforelasticsearch.notifications.resthandler

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import com.amazon.opendistroforelasticsearch.notifications.action.SendAction
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.rest.BytesRestResponse
import org.elasticsearch.rest.RestChannel
import org.elasticsearch.rest.RestHandler.Route
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestRequest.Method.POST
import org.elasticsearch.rest.RestStatus

/**
 * Rest handler for sending notification.
 * This handler [SendAction] for sending notification.
 */
internal class SendRestHandler : PluginRestHandler() {

    internal companion object {
        private val log by logger(SendRestHandler::class.java)
        const val SEND_BASE_URI = "$PLUGIN_BASE_URI/send"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String = "send"

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            Route(POST, SEND_BASE_URI)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        val handler = SendAction(request, client, channel)
        when (request.method()) {
            POST -> handler.send()
            else -> channel.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
        }
    }
}
