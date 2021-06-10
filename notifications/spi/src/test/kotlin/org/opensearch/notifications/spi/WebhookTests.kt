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
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.opensearch.notifications.spi.channel.client.ChannelHttpClient
import org.opensearch.notifications.spi.message.WebhookMessage
import org.opensearch.notifications.spi.model.ChannelMessageResponse
import org.opensearch.notifications.spi.model.DestinationType
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.rest.RestStatus

internal class WebhookTests {
    @Test
    @Throws(Exception::class)
    fun `test webhook message null entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = ChannelMessageResponse(statusText = "{}", statusCode = RestStatus.OK)

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)

        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(null).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = ChannelHttpClient()
        httpClient.setHttpClient(mockHttpClient)

        val message = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val webhookMessage = WebhookMessage(
            url = url,
            title = "TODO",
            configType = DestinationType.Chime,
            messageContent = MessageContent(textDescription = message),
            channelId = "channelId"
        )
        val actualChimeResponse: ChannelMessageResponse = Notification.sendMessage(webhookMessage)

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }

    @Test
    @Throws(Exception::class)
    fun `test webhook message empty entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = ChannelMessageResponse(statusText = "", statusCode = RestStatus.OK)

        val httpResponse: CloseableHttpResponse = EasyMock.createMock(CloseableHttpResponse::class.java)
        EasyMock.expect(mockHttpClient.execute(EasyMock.anyObject(HttpPost::class.java))).andReturn(httpResponse)
        val mockStatusLine: BasicStatusLine = EasyMock.createMock(BasicStatusLine::class.java)
        EasyMock.expect(httpResponse.statusLine).andReturn(mockStatusLine)
        EasyMock.expect(httpResponse.entity).andReturn(StringEntity("")).anyTimes()
        EasyMock.expect(mockStatusLine.statusCode).andReturn(RestStatus.OK.status)
        EasyMock.replay(mockHttpClient)
        EasyMock.replay(httpResponse)
        EasyMock.replay(mockStatusLine)

        val httpClient = ChannelHttpClient()
        httpClient.setHttpClient(mockHttpClient)

        val message = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"
        val webhookMessage = WebhookMessage(
            url = url,
            title = "TODO",
            configType = DestinationType.Chime,
            messageContent = MessageContent(textDescription = message),
            channelId = "channelId"
        )

        val actualChimeResponse: ChannelMessageResponse = Notification.sendMessage(webhookMessage)

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUrlMissingMessage() {
        try {
            WebhookMessage(
                url = "",
                title = "title",
                configType = DestinationType.Chime,
                messageContent = MessageContent(textDescription = "message"),
                channelId = "channelId"
            )
        } catch (ex: Exception) {
            assertEquals("url is invalid or empty", ex.message)
            throw ex
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testContentMissingMessage() {
        try {
            WebhookMessage(
                url = "test.com",
                title = "title",
                configType = DestinationType.Chime,
                messageContent = MessageContent(textDescription = ""),
                channelId = "channelId"
            )
        } catch (ex: Exception) {
            assertEquals("text message part is null or empty", ex.message)
            throw ex
        }
    }
}
