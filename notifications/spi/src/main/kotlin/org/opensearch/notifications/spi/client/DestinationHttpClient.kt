/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.notifications.spi.client

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentType
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.spi.model.destination.WebhookDestination
import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.OpenForTesting
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.notifications.spi.utils.string
import org.opensearch.rest.RestStatus
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Collections
import kotlin.collections.HashSet

/**
 * This class handles the connections to the given Destination.
 */
class DestinationHttpClient {

    private val httpClient: CloseableHttpClient

    constructor() {
        this.httpClient = createHttpClient()
    }
    @OpenForTesting
    constructor(httpClient: CloseableHttpClient) {
        this.httpClient = httpClient
    }

    companion object {
        private val log by logger(DestinationHttpClient::class.java)
        /**
         * all valid response status
         */
        private val VALID_RESPONSE_STATUS = Collections.unmodifiableSet(
            HashSet(
                listOf(
                    RestStatus.OK.status, RestStatus.CREATED.status, RestStatus.ACCEPTED.status,
                    RestStatus.NON_AUTHORITATIVE_INFORMATION.status, RestStatus.NO_CONTENT.status,
                    RestStatus.RESET_CONTENT.status, RestStatus.PARTIAL_CONTENT.status,
                    RestStatus.MULTI_STATUS.status
                )
            )
        )

        private fun createHttpClient(): CloseableHttpClient {
            val config: RequestConfig = RequestConfig.custom()
                .setConnectTimeout(PluginSettings.connectionTimeout)
                .setConnectionRequestTimeout(PluginSettings.connectionTimeout)
                .setSocketTimeout(PluginSettings.socketTimeout)
                .build()
            val connectionManager = PoolingHttpClientConnectionManager()
            connectionManager.maxTotal = PluginSettings.maxConnections
            connectionManager.defaultMaxPerRoute = PluginSettings.maxConnectionsPerRoute

            return HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager)
                .setRetryHandler(DefaultHttpRequestRetryHandler())
                .useSystemProperties()
                .build()
        }
    }

    @Throws(Exception::class)
    fun execute(destination: WebhookDestination, message: MessageContent): String {
        var response: CloseableHttpResponse? = null
        return try {
            response = getHttpResponse(destination, message)
            validateResponseStatus(response)
            getResponseString(response)
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.entity)
            }
        }
    }

    @Throws(Exception::class)
    private fun getHttpResponse(destination: WebhookDestination, message: MessageContent): CloseableHttpResponse {
        var httpRequest: HttpRequestBase = HttpPost(destination.url)

        if (destination is CustomWebhookDestination) {
            httpRequest = constructHttpRequest(destination.method)
            if (destination.headerParams.isEmpty()) {
                // set default header
                httpRequest.setHeader("Content-type", "application/json")
            } else {
                for ((key, value) in destination.headerParams.entries) httpRequest.setHeader(key, value)
            }
        }

        val entity = StringEntity(buildRequestBody(destination, message), StandardCharsets.UTF_8)
        (httpRequest as HttpEntityEnclosingRequestBase).entity = entity

        return httpClient.execute(httpRequest)
    }

    private fun constructHttpRequest(method: String): HttpRequestBase {
        return when (method) {
            HttpPost.METHOD_NAME -> HttpPost()
            HttpPut.METHOD_NAME -> HttpPut()
            HttpPatch.METHOD_NAME -> HttpPatch()
            else -> throw IllegalArgumentException(
                "Invalid or empty method supplied. Only POST, PUT and PATCH are allowed"
            )
        }
    }

    @Throws(IOException::class)
    fun getResponseString(response: CloseableHttpResponse): String {
        val entity: HttpEntity = response.entity ?: return "{}"
        val responseString: String = EntityUtils.toString(entity)
        log.debug("Http response: $responseString")
        return responseString
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode: Int = response.statusLine.statusCode
        if (!VALID_RESPONSE_STATUS.contains(statusCode)) {
            throw IOException("Failed: $response")
        }
    }

    fun buildRequestBody(destination: WebhookDestination, message: MessageContent): String {
        val builder = XContentFactory.contentBuilder(XContentType.JSON)
        var keyName = "Content"
        // Slack webhook request body has required "text" as key name https://api.slack.com/messaging/webhooks
        if (destination is SlackDestination) keyName = "text"
        builder.startObject()
            .field(keyName, message.buildWebhookMessage())
            .endObject()
        return builder.string()
    }
}
