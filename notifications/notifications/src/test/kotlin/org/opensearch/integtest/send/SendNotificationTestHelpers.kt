/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.send

import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Loopback webhook receiver that records every request body it is sent.
 */
internal class MockWebhookServer {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    val receivedBodies = CopyOnWriteArrayList<String>()

    init {
        server.createContext("/") { exchange ->
            receivedBodies.add(exchange.requestBody.bufferedReader().readText())
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
    }

    fun start() = server.start()

    fun stop() = server.stop(1)

    fun url(path: String): String = "http://${server.address.hostString}:${server.address.port}$path"
}

internal fun webhookConfigJson(name: String, url: String): String = """
    {
        "config":{
            "name":"$name",
            "description":"webhook for the send api",
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

internal fun mutedWebhookConfigJson(name: String, url: String): String = """
    {
        "config":{
            "name":"$name",
            "description":"muted webhook for the send api",
            "config_type":"webhook",
            "is_enabled":false,
            "webhook":{
                "url":"$url",
                "header_params": {
                   "Content-type": "text/plain"
                }
            }
        }
    }
""".trimIndent()

internal fun sendNotificationRequestJson(
    text: String = "Your report is ready",
    extraFields: String = ""
): String = """
    {
        "event_source":{
            "title":"Weekly report",
            "reference_id":"report-1",
            "severity":"info",
            "tags":[]
        },
        "channel_message":{
            "text_description":"$text"
        }$extraFields
    }
""".trimIndent()
