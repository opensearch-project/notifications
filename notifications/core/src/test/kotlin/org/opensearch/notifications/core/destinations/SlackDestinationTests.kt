/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.destinations

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.core5.http.io.entity.StringEntity
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
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SlackDestination
import java.net.MalformedURLException
import java.util.stream.Stream

internal class SlackDestinationTests {
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

    @BeforeEach
    fun setup() {
        // Stubbing isHostInDenylist() so it doesn't attempt to resolve hosts that don't exist in the unit tests
        mockkStatic("org.opensearch.notifications.spi.utils.ValidationHelpersKt")
        every { org.opensearch.notifications.spi.utils.isHostInDenylist(any(), any()) } returns false
    }

    @Test
    fun `test Slack message null entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")

        val httpResponse = mockk<CloseableHttpResponse>()
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)

        every { httpResponse.code } returns RestStatus.OK.status
        every { httpResponse.entity } returns null
        EasyMock.replay(mockHttpClient)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualSlackResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualSlackResponse.statusCode)
    }

    @Test
    fun `test Slack message empty entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")

        val httpResponse = mockk<CloseableHttpResponse>()
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        every { httpResponse.code } returns RestStatus.OK.status
        every { httpResponse.entity } returns StringEntity("")
        EasyMock.replay(mockHttpClient)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualSlackResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualSlackResponse.statusCode)
    }

    @Test
    fun `test Slack message non-empty entity response`() {
        val responseContent = "It worked!"
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, responseContent)

        val httpResponse = mockk<CloseableHttpResponse>()
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        every { httpResponse.code } returns RestStatus.OK.status
        every { httpResponse.entity } returns StringEntity(responseContent)
        EasyMock.replay(mockHttpClient)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualSlackResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualSlackResponse.statusCode)
    }

    @Test
    fun `test url missing should throw IllegalArgumentException with message`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            SlackDestination("")
        }
        assertEquals("url is null or empty", exception.message)
    }

    @Test
    fun testUrlInvalidMessage() {
        assertThrows<MalformedURLException> {
            ChimeDestination("invalidUrl")
        }
    }

    @ParameterizedTest
    @MethodSource("escapeSequenceToRaw")
    fun `test build webhook request body for slack should have title included and prevent escape`(
        escapeSequence: String,
        rawString: String
    ) {
        val httpClient = DestinationHttpClient()
        val title = "test Slack"
        val messageText = "line1${escapeSequence}line2"
        val url = "https://abc/com"
        val expectedRequestBody = """{"text":"$title\n\nline1${rawString}line2"}"""
        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)
        val actualRequestBody = httpClient.buildRequestBody(destination, message)
        assertEquals(expectedRequestBody, actualRequestBody)
    }
}
