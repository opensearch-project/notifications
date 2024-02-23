/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import com.sun.net.httpserver.HttpServer
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.rest.RestRequest
import java.net.InetAddress
import java.net.InetSocketAddress

internal class MaxHTTPResponseSizeIT : PluginRestTestCase() {
    fun `test HTTP response has truncated size`() {
        // update max http response size setting
        val updateSettingJsonString = """
        {
            "persistent": {
                "opensearch.notifications.core.max_http_response_size": "10"
            }
        }
        """.trimIndent()

        val updateSettingsResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "/_cluster/settings",
            updateSettingJsonString,
            RestStatus.OK.status
        )
        Assert.assertNotNull(updateSettingsResponse)
        logger.info("update settings response: $updateSettingsResponse")
        Thread.sleep(1000)

        val title = "test custom webhook"
        val messageText = "{\"Content\":\"sample message\"}"
        val url = "http://${server.address.hostString}:${server.address.port}/webhook"

        val destination = CustomWebhookDestination(url, mapOf("headerKey" to "headerValue"), "POST")
        val message = MessageContent(title, messageText)

        val actualCustomWebhookResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        logger.info("response: ${actualCustomWebhookResponse.statusText}")
    }

    companion object {
        private lateinit var server: HttpServer

        @JvmStatic
        @BeforeClass
        fun setupWebhook() {
            server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)

            server.createContext("/webhook") {
                val response = "This is a longer than usual response that should be truncated"
                it.sendResponseHeaders(200, response.length.toLong())
                val os = it.responseBody
                os.write(response.toByteArray())
                os.close()
            }

            server.start()
        }

        @JvmStatic
        @AfterClass
        fun stopMockServer() {
            server.stop(1)
        }
    }
}
