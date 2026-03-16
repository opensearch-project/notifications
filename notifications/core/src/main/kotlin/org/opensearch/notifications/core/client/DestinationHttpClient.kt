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
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.CharArrayBuffer
import org.apache.http.util.EntityUtils
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
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
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

        private const val DEFAULT_CHAR_BUFFER_SIZE = 1024
        private const val DEFAULT_BYTE_BUFFER_SIZE = 4096

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

        private val CONTENT_TYPE_MAP = Collections.unmodifiableMap(
            mapOf(
                ContentType.APPLICATION_ATOM_XML.mimeType to ContentType.APPLICATION_ATOM_XML,
                ContentType.APPLICATION_FORM_URLENCODED.mimeType to ContentType.APPLICATION_FORM_URLENCODED,
                ContentType.APPLICATION_JSON.mimeType to ContentType.APPLICATION_JSON,
                ContentType.APPLICATION_SVG_XML.mimeType to ContentType.APPLICATION_SVG_XML,
                ContentType.APPLICATION_XHTML_XML.mimeType to ContentType.APPLICATION_XHTML_XML,
                ContentType.APPLICATION_XML.mimeType to ContentType.APPLICATION_XML,
                ContentType.MULTIPART_FORM_DATA.mimeType to ContentType.MULTIPART_FORM_DATA,
                ContentType.TEXT_HTML.mimeType to ContentType.TEXT_HTML,
                ContentType.TEXT_PLAIN.mimeType to ContentType.TEXT_PLAIN,
                ContentType.TEXT_XML.mimeType to ContentType.TEXT_XML
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
        val responseString = httpResponseToString(entity, PluginSettings.maxHttpResponseSize / 2) // Java char is 2 bytes
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

    private fun httpResponseToString(entity: HttpEntity, maxResultLength: Int): String? {
        if (entity == null) throw IllegalArgumentException("HttpEntity received was null")
        val contentType = if (entity.contentType != null) ContentType.parse(entity.contentType.value) else null
        val contentLength = toContentLength(checkContentLength(entity).toInt())
        val inStream = entity.content ?: return null
        var charset: Charset? = null
        if (contentType != null) {
            charset = contentType.charset
            if (charset == null) {
                charset = CONTENT_TYPE_MAP[contentType.mimeType]?.charset
            }
        }
        return toCharArrayBuffer(inStream, contentLength, charset, maxResultLength).toString()
    }

    private fun checkContentLength(entity: HttpEntity): Long {
        if (entity.contentLength < -1 || entity.contentLength > Int.MAX_VALUE) {
            throw IllegalArgumentException("HTTP entity too large to be buffered in memory: ${entity.contentLength} is out of range [-1, ${Int.MAX_VALUE}]")
        }
        return entity.contentLength
    }

    private fun toContentLength(contentLength: Int): Int {
        return if (contentLength < 0) DEFAULT_BYTE_BUFFER_SIZE else contentLength
    }

    private fun toCharArrayBuffer(inStream: InputStream, contentLength: Int, charset: Charset?, maxResultLength: Int): CharArrayBuffer {
        require(maxResultLength > 0)
        val actualCharSet = charset ?: StandardCharsets.UTF_8
        val buf = CharArrayBuffer(minOf(maxResultLength, if (contentLength > 0) contentLength else DEFAULT_CHAR_BUFFER_SIZE))
        val reader = InputStreamReader(inStream, actualCharSet)
        val tmp = CharArray(DEFAULT_CHAR_BUFFER_SIZE)
        var chReadCount: Int
        while (reader.read(tmp).also { chReadCount = it } != -1 && buf.length < maxResultLength) {
            buf.append(tmp, 0, chReadCount)
        }
        buf.setLength(minOf(buf.length, maxResultLength))
        return buf
    }
}
