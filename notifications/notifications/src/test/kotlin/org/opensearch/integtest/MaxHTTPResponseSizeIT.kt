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
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.rest.RestRequest
import java.net.InetAddress
import java.net.InetSocketAddress

internal class MaxHTTPResponseSizeIT : PluginRestTestCase() {
    fun `test HTTP response has truncated size`() {
        // update max http response size setting
        val updateSettingJsonString = """
        {
            "transient": {
                "opensearch.notifications.core.max_http_response_size": "8"
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

        val url = "http://${server.address.hostString}:${server.address.port}/webhook"

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
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
            "",
            RestStatus.OK.status
        )

        logger.info("response: $sendResponse")

        val statusText = sendResponse.getAsJsonArray("status_list")[0].asJsonObject["delivery_status"].asJsonObject["status_text"].asString

        // we set the max HTTP response size to 8 bytes, which means the expected string length of the response is 8 / 2 (bytes per Java char) = 4
        Assert.assertEquals(4, statusText.length)
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
