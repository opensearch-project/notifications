/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.send

import com.google.gson.JsonArray
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.opensearch.client.RestClient
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.NOTIFICATION_NO_ACCESS_ROLE
import org.opensearch.integtest.NOTIFICATION_SEND_ACCESS
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.integtest.ROLE_TO_PERMISSION_MAPPING
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest

/**
 * Authorization of the send API with the security plugin, with and without backend-role filtering.
 */
internal class SendNotificationAccessIT : PluginRestTestCase() {

    private val password = randomAlphaOfLength(6) + "_" + randomIntBetween(1000, 10000) + "!" + randomAlphaOfLength(10)
    private fun sendUrl(configId: String) = "$PLUGIN_BASE_URI/feature/send/$configId"

    private fun createWebhookConfig(client: RestClient = client()): String {
        val configId = createConfigWithRequestJsonString(
            webhookConfigJson("send api access webhook", webhook.url("/access")),
            client
        )
        Thread.sleep(1000)
        return configId
    }

    private fun enableBackendRoleFiltering() {
        updateClusterSettings(
            ClusterSetting("persistent", "opensearch.notifications.general.filter_by_backend_roles", "true"),
            adminClient()
        )
    }

    private fun createRole(name: String, permissions: List<String>) {
        val request = org.opensearch.client.Request("PUT", "/_plugins/_security/api/roles/$name")
        request.setJsonEntity(
            """
            {
                "cluster_permissions": [${permissions.joinToString(",") { "\"$it\"" }}],
                "tenant_permissions": []
            }
            """.trimIndent()
        )
        adminClient().performRequest(request)
    }

    fun `test send succeeds with only the send permission`() {
        val user = "sendOnlyUser"
        createUserWithCustomRole(user, password, NOTIFICATION_SEND_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_SEND_ACCESS])
        val userClient = buildUserClient(user, password)
        try {
            val configId = createWebhookConfig()

            val sendResponse = executeRequest(
                RestRequest.Method.POST.name,
                sendUrl(configId),
                sendNotificationRequestJson("Sent with only the send permission"),
                RestStatus.OK.status,
                userClient
            )

            val deliveryStatus = (sendResponse.get("status_list") as JsonArray).get(0).asJsonObject
                .get("delivery_status").asJsonObject
            Assert.assertEquals("200", deliveryStatus.get("status_code").asString)
            Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent with only the send permission") })
        } finally {
            userClient.close()
            deleteUserWithCustomRole(user, NOTIFICATION_SEND_ACCESS)
        }
    }

    fun `test send is forbidden without the send permission`() {
        val user = "noSendUser"
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])
        val userClient = buildUserClient(user, password)
        try {
            val configId = createWebhookConfig()

            executeRequest(
                RestRequest.Method.POST.name,
                sendUrl(configId),
                sendNotificationRequestJson("Must not be delivered"),
                RestStatus.FORBIDDEN.status,
                userClient
            )
            Assert.assertFalse(webhook.receivedBodies.any { it.contains("Must not be delivered") })
        } finally {
            userClient.close()
            deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
        }
    }

    fun `test send honours backend role filtering on the channel`() {
        val role = "notifications_send_backend_roles"
        val owner = "sendChannelOwner"
        val teammate = "sendTeammate"
        val outsider = "sendOutsider"
        createRole(role, listOf("cluster:admin/opensearch/notifications/*"))
        createUser(owner, password, arrayOf("engineering"))
        createUser(teammate, password, arrayOf("engineering"))
        createUser(outsider, password, arrayOf("marketing"))
        createUserRolesMapping(role, arrayOf(owner, teammate, outsider))
        enableBackendRoleFiltering()

        val ownerClient = buildUserClient(owner, password)
        val teammateClient = buildUserClient(teammate, password)
        val outsiderClient = buildUserClient(outsider, password)
        try {
            val configId = createWebhookConfig(ownerClient)

            executeRequest(
                RestRequest.Method.POST.name,
                sendUrl(configId),
                sendNotificationRequestJson("Sent by a teammate"),
                RestStatus.OK.status,
                teammateClient
            )
            Assert.assertTrue(webhook.receivedBodies.any { it.contains("Sent by a teammate") })

            executeRequest(
                RestRequest.Method.POST.name,
                sendUrl(configId),
                sendNotificationRequestJson("Sent by an outsider"),
                RestStatus.FORBIDDEN.status,
                outsiderClient
            )
            Assert.assertFalse(webhook.receivedBodies.any { it.contains("Sent by an outsider") })

            // A user context in the body cannot borrow the owner's backend roles.
            executeRequest(
                RestRequest.Method.POST.name,
                sendUrl(configId),
                sendNotificationRequestJson(
                    "Sent with a forged context",
                    ",\"context\":\"$owner|engineering|$role\""
                ),
                RestStatus.BAD_REQUEST.status,
                outsiderClient
            )
            Assert.assertFalse(webhook.receivedBodies.any { it.contains("Sent with a forged context") })
        } finally {
            ownerClient.close()
            teammateClient.close()
            outsiderClient.close()
            deleteUserRolesMapping(role)
            deleteCustomRole(role)
            deleteUser(owner)
            deleteUser(teammate)
            deleteUser(outsider)
        }
    }

    companion object {
        private val webhook = MockWebhookServer()

        @BeforeClass
        @JvmStatic
        fun setup() {
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            org.junit.Assume.assumeFalse(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
            webhook.start()
        }

        @AfterClass
        @JvmStatic
        fun stopMockServer() {
            webhook.stop()
        }
    }
}
