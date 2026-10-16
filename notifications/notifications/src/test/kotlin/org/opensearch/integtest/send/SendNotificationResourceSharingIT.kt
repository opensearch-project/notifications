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
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.integtest.getCreateNotificationRequestJsonString
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
        enableSendApi()
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

    fun `test user the channel is shared with read_only cannot send`() {
        val configId = createWebhookConfig()
        share(configId, "notifications_read_only", "users", otherUser)
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent with read only access"),
            RestStatus.FORBIDDEN.status,
            otherClient!!
        )
        Assert.assertFalse(webhook.receivedBodies.any { it.contains("Sent with read only access") })
    }

    /**
     * The test route delivers through the in-process send path that Alerting and ISM use. That path must keep
     * working for a channel shared with the caller by user name, which a per-channel check on it would break.
     */
    fun `test in-process send path still delivers through a channel shared by user name`() {
        val configId = createWebhookConfig()
        share(configId, "notifications_read_write", "users", otherUser)
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/feature/test/$configId",
            "",
            RestStatus.OK.status,
            otherClient!!
        )
    }

    fun `test user the channel is shared with send_only can send but not change it`() {
        val configId = createWebhookConfig()
        share(configId, "notifications_send_only", "users", otherUser)
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(configId),
            sendNotificationRequestJson("Sent with send only access"),
            RestStatus.OK.status,
            otherClient!!
        )
        Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent with send only access") })

        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            webhookConfigJson("renamed by a send only user", webhook.url("/shared")),
            RestStatus.FORBIDDEN.status,
            otherClient!!
        )
        executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            otherClient!!
        )
    }

    /** An email channel with no direct recipients, so a send never reaches the SMTP host of the test account. */
    private fun createEmailChannel(accountId: String, groupIds: List<String>): String {
        val channelId = createConfigWithRequestJsonString(
            """
            {
                "config":{
                    "name":"send api email",
                    "description":"email channel for the send api",
                    "config_type":"email",
                    "is_enabled":true,
                    "email":{
                        "email_account_id":"$accountId",
                        "recipient_list":[],
                        "email_group_id_list":[${groupIds.joinToString { "\"$it\"" }}]
                    }
                }
            }
            """.trimIndent(),
            ownerClient!!
        )
        share(channelId, "notifications_send_only", "users", otherUser)
        return channelId
    }

    private fun createOwnerConfig(nameSubstring: String, configType: ConfigType): String =
        createConfigWithRequestJsonString(getCreateNotificationRequestJsonString(nameSubstring, configType, true), ownerClient!!)

    fun `test send through a shared email channel reports the account the caller cannot access`() {
        val accountId = createOwnerConfig("send api account", ConfigType.SMTP_ACCOUNT)
        val groupId = createOwnerConfig("send api group", ConfigType.EMAIL_GROUP)
        share(groupId, "notifications_send_only", "users", otherUser)
        val channelId = createEmailChannel(accountId, listOf(groupId))
        Thread.sleep(1000)

        val response = executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(channelId),
            sendNotificationRequestJson("Sent without access to the account"),
            RestStatus.FORBIDDEN.status,
            otherClient!!
        )
        Assert.assertTrue(response.toString().contains("Access denied for sender $accountId"))
    }

    fun `test send through a shared email channel reports each group that is not accessible or no longer exists`() {
        val accountId = createOwnerConfig("send api account", ConfigType.SMTP_ACCOUNT)
        share(accountId, "notifications_send_only", "users", otherUser)
        val unsharedGroupId = createOwnerConfig("send api unshared group", ConfigType.EMAIL_GROUP)
        val deletedGroupId = createOwnerConfig("send api deleted group", ConfigType.EMAIL_GROUP)
        val channelId = createEmailChannel(accountId, listOf(unsharedGroupId, deletedGroupId))
        executeRequest(RestRequest.Method.DELETE.name, "$PLUGIN_BASE_URI/configs/$deletedGroupId", "", RestStatus.OK.status, ownerClient!!)
        Thread.sleep(1000)

        // Each group gets its own status instead of one group failing the whole send
        val response = executeRequest(
            RestRequest.Method.POST.name,
            sendUrl(channelId),
            sendNotificationRequestJson("Sent to groups the caller cannot use"),
            RestStatus.MULTI_STATUS.status,
            otherClient!!
        ).toString()
        Assert.assertTrue(response.contains("Access denied for recipient $unsharedGroupId"))
        Assert.assertTrue(response.contains("Recipient $deletedGroupId not found"))
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
