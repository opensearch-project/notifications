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

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.rest.BaseRestHandler
import org.elasticsearch.rest.BaseRestHandler.RestChannelConsumer
import org.elasticsearch.rest.BytesRestResponse
import org.elasticsearch.rest.RestChannel
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestStatus
import java.io.IOException

/**
 * Plugin Rest handler which executes the request in async scope.
 */
internal abstract class PluginRestHandler : BaseRestHandler() {
    internal companion object {
        protected val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private val log by logger(PluginRestHandler::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        log.info("$LOG_PREFIX:prepareRequest-${request.method()}=${request.uri()}")
        return RestChannelConsumer { executeRequestInScope(request, client, it) }
    }

    /**
     * Execute the Rest request in scope. To be executed asynchronously
     * @param request the request to execute
     * @param client client for executing actions on the local node
     * @param channel Rest channel to send response to
     */
    @Suppress("TooGenericExceptionCaught")
    private fun executeRequestInScope(request: RestRequest, client: NodeClient, channel: RestChannel) {
        scope.launch {
            try {
                executeRequest(request, client, channel)
            } catch (exception: IllegalArgumentException) {
                log.warn("executeRequestInScope:", exception)
                channel.sendResponse(BytesRestResponse(RestStatus.BAD_REQUEST, exception.message))
            } catch (exception: IllegalStateException) {
                log.warn("executeRequestInScope:", exception)
                channel.sendResponse(BytesRestResponse(RestStatus.SERVICE_UNAVAILABLE, exception.message))
            } catch (exception: IOException) {
                log.error("executeRequestInScope: Uncaught IOException:", exception)
                channel.sendResponse(BytesRestResponse(RestStatus.FAILED_DEPENDENCY, exception.message))
            } catch (exception: Exception) {
                log.error("executeRequestInScope: Uncaught Exception:", exception)
                channel.sendResponse(BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, exception.message))
            }
        }
    }

    /**
     * Execute the Rest request
     * @param request the request to execute
     * @param client client for executing actions on the local node
     * @param channel Rest channel to send response to
     */
    abstract fun executeRequest(request: RestRequest, client: NodeClient, channel: RestChannel)
}
