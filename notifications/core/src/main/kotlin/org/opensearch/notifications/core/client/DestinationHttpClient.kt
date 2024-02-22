/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

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
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentType
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.core.utils.string
import org.opensearch.notifications.core.utils.validateUrlHost
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.spi.model.destination.WebhookDestination
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
                .disableRedirectHandling()
                .build()
        }
    }

    @Throws(Exception::class)
    fun execute(destination: WebhookDestination, message: MessageContent, referenceId: String): String {
        var response: CloseableHttpResponse? = null
        return try {
            // validate webhook url against host_deny_list in plugin settings
            validateUrlHost(destination.url, PluginSettings.hostDenyList)
            response = getHttpResponse(destination, message)
            validateResponseStatus(response)
            val responseString = getResponseString(response)
            log.debug("Http response for id $referenceId: $responseString")
            responseString
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
            httpRequest = constructHttpRequest(destination.method, destination.url)
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

    private fun constructHttpRequest(method: String, url: String): HttpRequestBase {
        return when (method) {
            HttpPost.METHOD_NAME -> HttpPost(url)
            HttpPut.METHOD_NAME -> HttpPut(url)
            HttpPatch.METHOD_NAME -> HttpPatch(url)
            else -> throw IllegalArgumentException(
                "Invalid or empty method supplied. Only POST, PUT and PATCH are allowed"
            )
        }
    }

    @Throws(IOException::class)
    fun getResponseString(response: CloseableHttpResponse): String {
        val entity: HttpEntity = response.entity ?: return "{}"
        val responseString = EntityUtils.toString(entity, PluginSettings.maxHttpResponseSize / 2) // Java char is 2 bytes
        // DeliveryStatus need statusText must not be empty, convert empty response to {}
        return if (responseString.isNullOrEmpty()) "{}" else responseString
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode: Int = response.statusLine.statusCode
        if (!VALID_RESPONSE_STATUS.contains(statusCode)) {
            throw IOException("Failed: ${EntityUtils.toString(response.entity)}")
        }
    }

    fun buildRequestBody(destination: WebhookDestination, message: MessageContent): String {
        val builder = XContentFactory.contentBuilder(XContentType.JSON)
        val keyName = when (destination) {
            // Slack webhook request body has required "text" as key name https://api.slack.com/messaging/webhooks
            // Chime webhook request body has required "Content" as key name
            // Customer webhook allows input as json or plain text, so we just return the message as it is
            is SlackDestination -> "text"
            is ChimeDestination -> "Content"
            is CustomWebhookDestination -> return message.textDescription
            else -> throw IllegalArgumentException(
                "Invalid destination type is provided, Only Slack, Chime and CustomWebhook are allowed"
            )
        }

        builder.startObject()
            .field(keyName, message.buildMessageWithTitle())
            .endObject()
        return builder.string()
    }
}
