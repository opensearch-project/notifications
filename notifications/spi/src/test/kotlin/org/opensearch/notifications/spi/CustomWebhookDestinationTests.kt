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
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.easymock.EasyMock
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.opensearch.notifications.spi.client.DestinationHttpClient
import org.opensearch.notifications.spi.factory.DestinationFactoryProvider
import org.opensearch.notifications.spi.factory.WebhookDestinationFactory
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.rest.RestStatus
import java.net.MalformedURLException

@RunWith(Parameterized::class)
internal class CustomWebhookDestinationTests {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Param: {0}={1}")
        fun params(): Array<Array<Any>> {
            return arrayOf(
                arrayOf("POST", HttpPost::class.java),
                arrayOf("PUT", HttpPut::class.java),
                arrayOf("PATCH", HttpPatch::class.java)
            )
        }
    }

    @JvmField
    @Parameterized.Parameter(0)
    var method: String = ""

    @JvmField
    @Parameterized.Parameter(1)
    var expectedHttpClass: Class<HttpUriRequest>? = null

    @Test
    @Throws(Exception::class)
    fun `test custom webhook message null entity response`() {
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
        val webhookDestinationFactory = WebhookDestinationFactory(httpClient)
        DestinationFactoryProvider.destinationFactoryMap = mapOf(
            DestinationType.CUSTOMWEBHOOK to webhookDestinationFactory
        )

        val title = "test custom webhook"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message)

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @Test
    @Throws(Exception::class)
    fun `test custom webhook message empty entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "")

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
        val webhookDestinationFactory = WebhookDestinationFactory(httpClient)
        DestinationFactoryProvider.destinationFactoryMap = mapOf(
            DestinationType.CUSTOMWEBHOOK to webhookDestinationFactory
        )

        val title = "test custom webhook"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message)

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @Test
    @Throws(Exception::class)
    fun `test custom webhook message non-empty entity response`() {
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
        val webhookDestinationFactory = WebhookDestinationFactory(httpClient)
        DestinationFactoryProvider.destinationFactoryMap = mapOf(
            DestinationType.CUSTOMWEBHOOK to webhookDestinationFactory
        )

        val title = "test custom webhook"
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present\"}"
        val url = "https://abc/com"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message)

        assertEquals(expectedWebhookResponse.statusText, actualCustomWebhookResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualCustomWebhookResponse.statusCode)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Test missing url will throw exception`() {
        try {
            CustomWebhookDestination("", mapOf("headerKey" to "headerValue"), method)
        } catch (ex: Exception) {
            assertEquals("url is null or empty", ex.message)
            throw ex
        }
    }

    @Test
    fun testUrlInvalidMessage() {
        assertThrows<MalformedURLException> {
            CustomWebhookDestination("invalidUrl", mapOf("headerKey" to "headerValue"), method)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Test invalid method type will throw exception`() {
        try {
            CustomWebhookDestination("https://abc/com", mapOf("headerKey" to "headerValue"), "GET")
        } catch (ex: Exception) {
            assertEquals("Invalid method supplied. Only POST, PUT and PATCH are allowed", ex.message)
            throw ex
        }
    }

    @Test
    fun `test build request body for custom webhook should have title included and prevent escape`() {
        val httpClient = DestinationHttpClient()
        val title = "test custom webhook"
        val messageText = "line1\nline2"
        val url = "https://abc/com"
        val expectedRequestBody = """{"Content":"$title\n\nline1\nline2"}"""
        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), method)
        val message = MessageContent(title, messageText)
        val actualRequestBody = httpClient.buildRequestBody(destination, message)
        assertEquals(expectedRequestBody, actualRequestBody)
    }
}
