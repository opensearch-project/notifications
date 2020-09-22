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
import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import com.amazon.opendistroforelasticsearch.notifications.action.SendAction
import org.apache.logging.log4j.LogManager
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.rest.BaseRestHandler
import org.elasticsearch.rest.BaseRestHandler.RestChannelConsumer
import org.elasticsearch.rest.RestHandler.Route
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestRequest.Method.POST
import java.io.IOException

class SendRestHandler(private val settings: Settings) : BaseRestHandler() {
    private val log = LogManager.getLogger(javaClass)
    companion object {
        const val SEND_BASE_URI = "$PLUGIN_BASE_URI/send"
    }

    override fun getName(): String = "send"

    override fun routes(): List<Route> {
        return listOf(
                Route(POST, SEND_BASE_URI)
        )
    }

    @Throws(IOException::class)
    @Suppress("SpreadOperator") // There is no way around dealing with java vararg without spread operator.
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        log.debug("$PLUGIN_NAME:prepareRequest")
        return RestChannelConsumer {
            SendAction(settings, request, client, it).send()
        }
    }
}
