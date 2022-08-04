/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.destinations

import io.mockk.every
import io.mockk.mockkStatic
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.easymock.EasyMock
import org.junit.Before
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.core.client.DestinationHttpClient
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.WebhookDestinationTransport
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.rest.RestStatus
import java.net.MalformedURLException
import java.util.stream.Stream

internal class ChimeDestinationTests {
    companion object {
        @JvmStatic
        fun escapeSequenceToRaw(): Stream<Arguments> =
            Stream.of(
                Arguments.of("\n", """\n"""),
                Arguments.of("\t", """\t"""),
                Arguments.of("\b", """\b"""),
                Arguments.of("\r", """\r"""),
                Arguments.of("\"", """\""""),
            )
    }

    @Before
    fun setup() {
        // Stubbing isHostInDenylist() so it doesn't attempt to resolve hosts that don't exist in the unit tests
        mockkStatic("org.opensearch.notifications.spi.utils.ValidationHelpersKt")
        every { org.opensearch.notifications.spi.utils.isHostInDenylist(any(), any()) } returns false
    }

    @Test
    fun `test chime message null entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")
        // TODO replace EasyMock in all UTs with mockk which fits Kotlin better
        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)

        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(null).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.CHIME to webhookDestinationTransport)

        val title = "test Chime"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member call out: " +
            "@All All Present member call out: @Present"
        val url = "https://abc/com"

        val destination = ChimeDestination(url)
        val message = MessageContent(title, messageText)

        val actualChimeResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }

    @Test
    fun `test chime message empty entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "")

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(StringEntity("")).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.CHIME to webhookDestinationTransport)

        val title = "test Chime"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member call out: " +
            "@All All Present member call out: @Present\"}"
        val url = "https://abc/com"

        val destination = ChimeDestination(url)
        val message = MessageContent(title, messageText)

        val actualChimeResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }

    @Test
    fun `test chime message non-empty entity response`() {
        val responseContent = "It worked!"
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, responseContent)

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(StringEntity(responseContent)).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.CHIME to webhookDestinationTransport)

        val title = "test Chime"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member call out: " +
            "@All All Present member call out: @Present\"}"
        val url = "https://abc/com"

        val destination = ChimeDestination(url)
        val message = MessageContent(title, messageText)

        val actualChimeResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }

    @Test
    fun `test url missing should throw IllegalArgumentException with message`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChimeDestination("")
        }
        assertEquals("url is null or empty", exception.message)
    }

    @Test
    fun testUrlInvalidMessage() {
        assertThrows<MalformedURLException> {
            ChimeDestination("invalidUrl")
        }
    }

    @Test
    fun `test content missing content should throw IllegalArgumentException`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            MessageContent("title", "")
        }
        assertEquals("text message part is null or empty", exception.message)
    }

    @ParameterizedTest
    @MethodSource("escapeSequenceToRaw")
    fun `test build request body for chime webhook should have title included and prevent escape`(
        escapeSequence: String,
        rawString: String
    ) {
        val httpClient = DestinationHttpClient()
        val title = "test chime webhook"
        val messageText = "line1${escapeSequence}line2"
        val url = "https://abc/com"
        val expectedRequestBody = """{"Content":"$title\n\nline1${rawString}line2"}"""
        val destination = ChimeDestination(url)
        val message = MessageContent(title, messageText)
        val actualRequestBody = httpClient.buildRequestBody(destination, message)
        assertEquals(expectedRequestBody, actualRequestBody)
    }
}
