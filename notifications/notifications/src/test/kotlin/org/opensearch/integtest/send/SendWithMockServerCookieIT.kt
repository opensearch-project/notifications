/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.send

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpServer
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.integtest.jsonify
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import java.net.InetAddress
import java.net.InetSocketAddress

internal class SendWithMockServerCookieIT : PluginRestTestCase() {

    fun `test webhook return with cookie set`() {
        val url = "http://${server.address.hostString}:${server.address.port}/webhook"
        logger.info("webhook url = {}", url)
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"test-webhook",
                "description":"this is a sample config description",
                "config_type":"webhook",
                "is_enabled":true,
                "webhook":{
                    "url":"$url",
                    "header_params": {
                       "Content-type": "text/plain"
                    }
                }
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // send test message
        var sendResponse = executeRequest(
            RestRequest.Method.POST.name, "$PLUGIN_BASE_URI/feature/test/$configId", "", RestStatus.OK.status
        )

        logger.info("sendResponse1={}", sendResponse)

        var deliveryStatus = (sendResponse.get("status_list") as JsonArray).get(0).asJsonObject
            .get("delivery_status") as JsonObject
        Assert.assertEquals(deliveryStatus.get("status_code").asString, "200")

        // send test message again with cookie set
        sendResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/feature/test/$configId", "", RestStatus.INTERNAL_SERVER_ERROR.status
        )

        logger.info("sendResponse2={}", sendResponse)

        val realResponse = sendResponse.get("error").asJsonObject.get("reason").asString
        deliveryStatus = jsonify(realResponse).get("event_status_list").asJsonArray.get(0).asJsonObject
            .get("delivery_status").asJsonObject
        Assert.assertEquals(
            deliveryStatus.get("status_text").asString,
            "Failed to send webhook message Failed: Unauthorized"
        )
    }

    fun `test webhook return with cookie disabled`() {
        // update settings
        executeRequest(
            RestRequest.Method.PUT.name, "/_cluster/settings",
            """
                {
                  "transient": {
                    "opensearch.notifications.core.webhook.disable_http_cookie": true
                  }
                }
            """.trimIndent(),
            RestStatus.OK.status,
            adminClient()
        )

        val url = "http://${server.address.hostString}:${server.address.port}/webhook"
        logger.info("webhook url = {}", url)
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"test-webhook",
                "description":"this is a sample config description",
                "config_type":"webhook",
                "is_enabled":true,
                "webhook":{
                    "url":"$url",
                    "header_params": {
                       "Content-type": "text/plain"
                    }
                }
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // send test message
        var sendResponse = executeRequest(
            RestRequest.Method.POST.name, "$PLUGIN_BASE_URI/feature/test/$configId", "", RestStatus.OK.status
        )

        logger.info("sendResponse1={}", sendResponse)

        var deliveryStatus = (sendResponse.get("status_list") as JsonArray).get(0).asJsonObject
            .get("delivery_status") as JsonObject
        Assert.assertEquals(deliveryStatus.get("status_code").asString, "200")

        // send test message again with cookie set
        sendResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/feature/test/$configId", "", RestStatus.OK.status
        )

        logger.info("sendResponse2={}", sendResponse)
    }

    companion object {
        private lateinit var server: HttpServer

        @JvmStatic
        @BeforeClass
        fun setupWebhook() {
            server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)

            server.createContext("/webhook") {
                val response = "test response"
                val cookies = it.requestHeaders["Cookie"]
                if (cookies != null && cookies.size > 0 && cookies[0].contains("sessionId=123456789")) {
                    it.sendResponseHeaders(401, response.toByteArray().size.toLong())
                    it.responseBody.write(response.toByteArray())
                    it.close()
                } else {
                    // Set a session cookie
                    val cookieValue = "sessionId=123456789; HttpOnly;"
                    // Add the "Set-Cookie" header to the response
                    it.responseHeaders.add("Set-Cookie", cookieValue)

                    // Send a simple response
                    it.sendResponseHeaders(200, response.toByteArray().size.toLong())
                    it.responseBody.write(response.toByteArray())
                    it.close()
                }
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
