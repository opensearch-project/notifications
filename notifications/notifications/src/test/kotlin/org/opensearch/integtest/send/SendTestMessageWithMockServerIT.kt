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
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import java.net.InetAddress
import java.net.InetSocketAddress

internal class SendTestMessageWithMockServerIT : PluginRestTestCase() {

    fun `test webhook return with empty entity`() {
        val url = "http://${server.address.hostString}:${server.address.port}/webhook"
        logger.info("webhook url = {}", url)
        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
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
        val sendResponse = executeRequest(
            RestRequest.Method.POST.name, "$PLUGIN_BASE_URI/feature/test/$configId", "", RestStatus.OK.status
        )

        logger.info(sendResponse)

        // verify failure response is with message
        val deliveryStatus = (sendResponse.get("status_list") as JsonArray).get(0).asJsonObject
            .get("delivery_status") as JsonObject
        Assert.assertEquals(deliveryStatus.get("status_code").asString, "200")
        Assert.assertEquals(deliveryStatus.get("status_text").asString, "{}")
    }

    companion object {
        private lateinit var server: HttpServer

        @JvmStatic
        @BeforeClass
        fun setupWebhook() {
            server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)

            server.createContext("/webhook") {
                it.sendResponseHeaders(200, -1)
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
