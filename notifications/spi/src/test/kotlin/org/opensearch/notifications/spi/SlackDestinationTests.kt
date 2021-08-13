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

package org.opensearch.notifications.spi

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.easymock.EasyMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.opensearch.notifications.spi.client.DestinationHttpClient
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.spi.transport.DestinationTransportProvider
import org.opensearch.notifications.spi.transport.WebhookDestinationTransport
import org.opensearch.rest.RestStatus
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

    @Test
    fun `test Slack message null entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")

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
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualSlackResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualSlackResponse.statusCode)
    }

    @Test
    fun `test Slack message empty entity response`() {
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
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message, "ref")

        assertEquals(expectedWebhookResponse.statusText, actualSlackResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualSlackResponse.statusCode)
    }

    @Test
    fun `test Slack message non-empty entity response`() {
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
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SLACK to webhookDestinationTransport)

        val title = "test Slack"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = SlackDestination(url)
        val message = MessageContent(title, messageText)

        val actualSlackResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message, "ref")

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
