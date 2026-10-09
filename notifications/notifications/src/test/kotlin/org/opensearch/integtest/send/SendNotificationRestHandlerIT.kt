/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.send

import com.google.gson.JsonArray
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest

internal class SendNotificationRestHandlerIT : PluginRestTestCase() {

    private fun sendUrl(configId: String) = "$PLUGIN_BASE_URI/feature/send/$configId"

    private fun createWebhookConfig(path: String): String {
        val configId = createConfigWithRequestJsonString(webhookConfigJson("send api webhook", webhook.url(path)))
        Assert.assertNotNull(configId)
        Thread.sleep(1000)
        return configId
    }

    fun `test send delivers the caller supplied message to the channel`() {
        val configId = createWebhookConfig("/first")

        val sendResponse = executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Delivered to the channel"),
            RestStatus.OK.status
        )

        val statusList = sendResponse.get("status_list") as JsonArray
        Assert.assertEquals(1, statusList.size())
        val status = statusList.get(0).asJsonObject
        Assert.assertEquals(configId, status.get("config_id").asString)
        Assert.assertEquals("200", status.get("delivery_status").asJsonObject.get("status_code").asString)
        Assert.assertEquals(1, webhook.receivedBodies.count { it.contains("Delivered to the channel") })
    }

    fun `test send rejects a user context in the request body`() {
        val configId = createWebhookConfig("/first")

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson(extraFields = ",\"context\":\"admin||all_access\""),
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test send rejects a channel list in the request body`() {
        val configId = createWebhookConfig("/first")

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson(extraFields = ",\"channel_id_list\":[\"$configId\"]"),
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test send reports a missing channel as not found`() {
        val sendResponse = executeRequest(
            RestRequest.Method.POST.name,
            sendUrl("does-not-exist"),
            sendNotificationRequestJson(),
            RestStatus.NOT_FOUND.status
        )

        Assert.assertNotNull(sendResponse.get("error").asJsonObject.get("reason").asString)
    }

    fun `test send requires an event source and a channel message`() {
        val configId = createWebhookConfig("/first")

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            """{"channel_message":{"text_description":"No event source"}}""",
            RestStatus.BAD_REQUEST.status
        )

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            """{"event_source":{"title":"t","reference_id":"r","severity":"info","tags":[]}}""",
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test send reports a muted channel as locked`() {
        val configId = createConfigWithRequestJsonString(
            mutedWebhookConfigJson("muted send api webhook", webhook.url("/muted"))
        )
        Thread.sleep(1000)

        val sendResponse = executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Must not be delivered to a muted channel"),
            RestStatus.LOCKED.status
        )

        Assert.assertNotNull(sendResponse.get("error").asJsonObject.get("reason").asString)
        Assert.assertFalse(webhook.receivedBodies.any { it.contains("Must not be delivered to a muted channel") })
    }

    companion object {
        private val webhook = MockWebhookServer()

        @JvmStatic
        @BeforeClass
        fun setupWebhook() {
            webhook.start()
        }

        @JvmStatic
        @AfterClass
        fun stopMockServer() {
            webhook.stop()
        }
    }
}
