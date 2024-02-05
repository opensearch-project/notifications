/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

import org.apache.hc.client5.http.classic.methods.HttpPatch
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.HttpResponse
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.apache.hc.core5.util.Timeout
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.MediaTypeRegistry
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.core.utils.string
import org.opensearch.notifications.core.utils.validateUrlHost
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.MicrosoftTeamsDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.spi.model.destination.WebhookDestination
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
                    RestStatus.OK.status,
                    RestStatus.CREATED.status,
                    RestStatus.ACCEPTED.status,
                    RestStatus.NON_AUTHORITATIVE_INFORMATION.status,
                    RestStatus.NO_CONTENT.status,
                    RestStatus.RESET_CONTENT.status,
                    RestStatus.PARTIAL_CONTENT.status,
                    RestStatus.MULTI_STATUS.status
                )
            )
        )

        private fun createHttpClient(): CloseableHttpClient {
            val config: RequestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(PluginSettings.connectionTimeout.toLong()))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(PluginSettings.connectionTimeout.toLong()))
                .setResponseTimeout(Timeout.ofMilliseconds(PluginSettings.socketTimeout.toLong()))
                .build()
            val connectionManager = PoolingHttpClientConnectionManager()
            connectionManager.maxTotal = PluginSettings.maxConnections
            connectionManager.defaultMaxPerRoute = PluginSettings.maxConnectionsPerRoute

            return HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager)
                .setRetryStrategy(DefaultHttpRequestRetryStrategy())
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
        var httpRequest: HttpUriRequestBase = HttpPost(destination.url)

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
        httpRequest.entity = entity

        return httpClient.execute(httpRequest)
    }

    private fun constructHttpRequest(method: String, url: String): HttpUriRequestBase {
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
        val responseString = EntityUtils.toString(entity)
        // DeliveryStatus need statusText must not be empty, convert empty response to {}
        return if (responseString.isNullOrEmpty()) "{}" else responseString
    }

    @Throws(IOException::class)
    private fun validateResponseStatus(response: HttpResponse) {
        val statusCode: Int = response.code
        if (!VALID_RESPONSE_STATUS.contains(statusCode)) {
            throw IOException("Failed: ${response.reasonPhrase}")
        }
    }

    fun buildRequestBody(destination: WebhookDestination, message: MessageContent): String {
        val builder = MediaTypeRegistry.contentBuilder(XContentType.JSON)
        val keyName = when (destination) {
            // Slack webhook request body has required "text" as key name https://api.slack.com/messaging/webhooks
            // Chime webhook request body has required "Content" as key name
            // Microsoft Teams webhook request body has required "text" as key name https://learn.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/what-are-webhooks-and-connectors
            // Customer webhook allows input as json or plain text, so we just return the message as it is
            is SlackDestination -> "text"
            is ChimeDestination -> "Content"
            is MicrosoftTeamsDestination -> "text"
            is CustomWebhookDestination -> return message.textDescription
            else -> throw IllegalArgumentException(
                "Invalid destination type is provided, Only Slack, Chime, Microsoft Teams and CustomWebhook are allowed"
            )
        }

        builder.startObject()
            .field(keyName, message.buildMessageWithTitle())
            .endObject()
        return builder.string()
    }
}
