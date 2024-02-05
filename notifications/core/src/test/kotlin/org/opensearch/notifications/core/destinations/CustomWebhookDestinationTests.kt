/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.destinations

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.easymock.EasyMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.core.client.DestinationHttpClient
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.WebhookDestinationTransport
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.DestinationType
import java.net.MalformedURLException
import java.util.stream.Stream

internal class CustomWebhookDestinationTests {
    companion object {
        @JvmStatic
        fun methodToHttpRequestType(): Stream<Arguments> =
            Stream.of(
                Arguments.of("POST", HttpPost::class.java),
                Arguments.of("PUT", HttpPut::class.java),
                Arguments.of("PATCH", HttpPatch::class.java)
            )

        @JvmStatic
        fun escapeSequenceToRaw(): Stream<Arguments> =
            Stream.of(
                Arguments.of("\n", """\n"""),
                Arguments.of("\t", """\t"""),
                Arguments.of("\b", """\b"""),
                Arguments.of("\r", """\r"""),
                Arguments.of("\"", """\"""")
            )
    }

    @BeforeEach
    fun setup() {
        // Stubbing isHostInDenylist() so it doesn't attempt to resolve hosts that don't exist in the unit tests
        mockkStatic("org.opensearch.notifications.spi.utils.ValidationHelpersKt")
        every { org.opensearch.notifications.spi.utils.isHostInDenylist(any(), any()) } returns false
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `test custom webhook message null entity response`(method: String, expectedHttpClass: Class<HttpUriRequest>) {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        EasyMock.expect(mockHttpClient.execute(EasyMock.isA(expectedHttpClass))).andReturn(httpResponse)

        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(null).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.CUSTOM_WEBHOOK to webhookDestinationTransport
        )

        val title = "test custom webhook"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `test custom webhook message empty entity response`(method: String, expectedHttpClass: Class<HttpUriRequest>) {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        EasyMock.expect(mockHttpClient.execute(EasyMock.isA(expectedHttpClass))).andReturn(httpResponse)

        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(StringEntity("")).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.CUSTOM_WEBHOOK to webhookDestinationTransport
        )

        val title = "test custom webhook"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `test custom webhook message non-empty entity response`(
        method: String,
        expectedHttpClass: Class<HttpUriRequest>
    ) {
        val responseContent = "It worked!"
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, responseContent)
        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        EasyMock.expect(mockHttpClient.execute(EasyMock.isA(expectedHttpClass))).andReturn(httpResponse)

        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(StringEntity(responseContent)).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.CUSTOM_WEBHOOK to webhookDestinationTransport
        )

        val title = "test custom webhook"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `Test missing url will throw exception`(method: String) {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            CustomWebhookDestination("", mapOf("headerKey" to "headerValue"), method)
        }
        assertEquals("url is null or empty", exception.message)
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `Custom webhook should throw exception if url is invalid`(method: String) {
        assertThrows<MalformedURLException> {
            CustomWebhookDestination("invalidUrl", mapOf("headerKey" to "headerValue"), method)
        }
    }

    @ParameterizedTest(name = "method {0} should return corresponding type of Http request object {1}")
    @MethodSource("methodToHttpRequestType")
    fun `Custom webhook should throw exception if url protocol is not http or https`(method: String) {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            CustomWebhookDestination("ftp://abc/com", mapOf("headerKey" to "headerValue"), method)
        }
        assertEquals("Invalid URL or unsupported", exception.message)
    }

    @Test
    fun `Test invalid method type will throw exception`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            CustomWebhookDestination("https://abc/com", mapOf("headerKey" to "headerValue"), "GET")
        }
        assertEquals("Invalid method supplied. Only POST, PUT and PATCH are allowed", exception.message)
    }

    @Test
    fun `test build request body for custom webhook`() {
        val httpClient = DestinationHttpClient()
        val title = "test custom webhook"
        val messageText = "{\"Customized Key\":\"some content\"}"
        val url = "https://abc/com"
        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), "POST")
        val message = MessageContent(title, messageText)
        val actualRequestBody = httpClient.buildRequestBody(destination, message)
        assertEquals(messageText, actualRequestBody)
    }

    @Test
    fun `test get response string`() {
        val httpClient = DestinationHttpClient()
        val response = mockk<CloseableHttpResponse>()
        every { response.entity } returns null
        var responseString = httpClient.getResponseString(response)
        assertEquals(responseString, "{}")

        every { response.entity } returns StringEntity("")
        responseString = httpClient.getResponseString(response)
        assertEquals(responseString, "{}")
    }
}
