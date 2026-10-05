/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.send

import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.opensearch.client.Request
import org.opensearch.client.RestClient
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest

/**
 * Authorization of the send API when channels are protected by resource sharing.
 * Every user holds the same cluster permissions, so only resource-level access differs.
 */
internal class SendNotificationResourceSharingIT : PluginRestTestCase() {

    private val role = "notifications_send_resource_sharing"
    private val ownerUser = "rs_send_owner"
    private val ownerPassword = randomAlphaOfLength(6) + "_" + randomIntBetween(1000, 10000) + "!" + randomAlphaOfLength(10)
    private val otherUser = "rs_send_other"
    private val otherPassword = randomAlphaOfLength(6) + "_" + randomIntBetween(1000, 10000) + "!" + randomAlphaOfLength(10)
    private var ownerClient: RestClient? = null
    private var otherClient: RestClient? = null
    private fun sendUrl(configId: String) = "$PLUGIN_BASE_URI/feature/send/$configId"

    @Before
    fun setupUsers() {
        val request = Request("PUT", "/_plugins/_security/api/roles/$role")
        request.setJsonEntity(
            """
            {
                "cluster_permissions": [
                    "cluster:admin/opensearch/notifications/*",
                    "cluster:admin/security/resource/*"
                ],
                "tenant_permissions": []
            }
            """.trimIndent()
        )
        adminClient().performRequest(request)
        createUser(ownerUser, ownerPassword, arrayOf("engineering"))
        createUser(otherUser, otherPassword, arrayOf("marketing"))
        createUserRolesMapping(role, arrayOf(ownerUser, otherUser))
        ownerClient = buildUserClient(ownerUser, ownerPassword)
        otherClient = buildUserClient(otherUser, otherPassword)
    }

    @After
    fun cleanupUsers() {
        ownerClient?.close()
        otherClient?.close()
        ownerClient = null
        otherClient = null
    }

    private fun createWebhookConfig(): String {
        val configId = createConfigWithRequestJsonString(
            webhookConfigJson("send api shared webhook", webhook.url("/shared")),
            ownerClient!!
        )
        Thread.sleep(1000)
        return configId
    }

    private fun share(resourceId: String, accessLevel: String, recipientType: String, recipient: String) {
        val request = Request("PUT", "/_plugins/_security/api/resource/share")
        request.setJsonEntity(
            """
            {
              "resource_id": "$resourceId",
              "resource_type": "notification_config",
              "share_with": {
                "$accessLevel": {
                    "$recipientType": ["$recipient"]
                }
              }
            }
            """.trimIndent()
        )
        Assert.assertEquals(200, ownerClient!!.performRequest(request).statusLine.statusCode)
    }

    fun `test owner can send through their own channel`() {
        val configId = createWebhookConfig()

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent by the owner"),
            RestStatus.OK.status,
            ownerClient!!
        )
        Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent by the owner") })
    }

    fun `test user without access to the channel cannot send`() {
        val configId = createWebhookConfig()

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent without access"),
            RestStatus.FORBIDDEN.status,
            otherClient!!
        )
        Assert.assertFalse(webhook.receivedBodies.any { it.contains("Sent without access") })
    }

    fun `test user the channel is shared with read_write can send`() {
        val configId = createWebhookConfig()
        share(configId, "notifications_read_write", "users", otherUser)
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent through a shared channel"),
            RestStatus.OK.status,
            otherClient!!
        )
        Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent through a shared channel") })
    }

    fun `test user whose backend role the channel is shared with can send`() {
        val configId = createWebhookConfig()
        share(configId, "notifications_read_write", "backend_roles", "marketing")
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent through a channel shared by backend role"),
            RestStatus.OK.status,
            otherClient!!
        )
        Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent through a channel shared by backend role") })
    }

    companion object {
        private val webhook = MockWebhookServer()

        @BeforeClass
        @JvmStatic
        fun setup() {
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            org.junit.Assume.assumeTrue(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
            webhook.start()
        }

        @AfterClass
        @JvmStatic
        fun stopMockServer() {
            webhook.stop()
        }
    }
}
