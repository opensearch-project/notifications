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

package org.opensearch.notifications.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.utils.contentParserNextToken
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.action.SendMessageAction
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.model.SendMessageRequest
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestStatus

/**
 * Rest handler for sending notification.
 * This handler [SendAction] for sending notification.
 */
internal class SendMessageRestHandler : BaseRestHandler() {

    internal companion object {
        const val SEND_BASE_URI = "$PLUGIN_BASE_URI/send"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String = "send_message"

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
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> RestChannelConsumer {
                Metrics.NOTIFICATIONS_SEND_MESSAGE_TOTAL.counter.increment()
                Metrics.NOTIFICATIONS_SEND_MESSAGE_INTERVAL_COUNT.counter.increment()
                client.execute(
                    SendMessageAction.ACTION_TYPE,
                    SendMessageRequest(request.contentParserNextToken()),
                    RestResponseToXContentListener(it)
                )
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
