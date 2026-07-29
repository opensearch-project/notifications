/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.opensearch.client.Request
import org.opensearch.client.RestClient
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.rest.RestRequest

/**
 * Integration tests for Resource Sharing feature with Notifications plugin.
 * Only runs when both security and resource_sharing are enabled.
 *
 * All users have the same cluster-level role (notifications_full_access) which grants
 * all notification actions + resource sharing. The tests verify that resource-level
 * access control (via access levels in resource-access-levels.yml) correctly restricts
 * what shared users can do regardless of their cluster permissions.
 */
class ResourceSharingNotificationIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            org.junit.Assume.assumeTrue(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
        }
    }

    private val notificationsFullAccessRole = "notifications_full_access"
    private val aliceUser = "rs_alice"
    private val alicePassword = "TopSecret_1234%Alice"
    private val bobUser = "rs_bob"
    private val bobPassword = "TopSecret_1234%Bobby"
    private val charlieUser = "rs_charlie"
    private val charliePassword = "TopSecret_1234%Charlie"
    private var aliceClient: RestClient? = null
    private var bobClient: RestClient? = null
    private var charlieClient: RestClient? = null

    @Before
    fun setupUsers() {
        if (aliceClient != null) return
        createNotificationsRole()
        createUser(aliceUser, alicePassword, arrayOf("engineering"))
        createUser(bobUser, bobPassword, arrayOf("marketing"))
        createUser(charlieUser, charliePassword, arrayOf("sales"))
        createUserRolesMapping(notificationsFullAccessRole, arrayOf(aliceUser, bobUser, charlieUser))

        aliceClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), aliceUser, alicePassword)
            .setSocketTimeout(60000).build()
        bobClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), bobUser, bobPassword)
            .setSocketTimeout(60000).build()
        charlieClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), charlieUser, charliePassword)
            .setSocketTimeout(60000).build()
    }

    @After
    fun cleanupClients() {
        aliceClient?.close()
        bobClient?.close()
        charlieClient?.close()
        aliceClient = null
        bobClient = null
        charlieClient = null
    }

    // --- Owner tests ---

    fun `test owner can perform all operations on their own config`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        // Owner can read
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        Assert.assertNotNull(getResponse)

        // Owner can update
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            buildUpdateJson("alice updated her own config"),
            RestStatus.OK.status,
            aliceClient!!
        )

        // Owner can share
        shareResource(aliceClient!!, configId, "notifications_read_only", bobUser)

        // Owner can delete
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
    }

    // --- Non-owner without sharing ---

    fun `test non-owner with full cluster role cannot access unshared config`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        // Bob has full cluster permissions but no resource-level access
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )

        // Bob cannot update
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            buildUpdateJson("attempted update"),
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )

        // Bob cannot delete
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )

        // Bob cannot share a resource they have no access to
        val shareRequest = Request("PUT", "/_plugins/_security/api/resource/share")
        shareRequest.setJsonEntity(buildShareJson(configId, "notifications_read_only", charlieUser))
        executeRequest(shareRequest, RestStatus.FORBIDDEN.status, bobClient!!)
    }

    // --- Isolation: DLS-based search filtering ---

    fun `test user only sees own and shared configs in list`() {
        val aliceConfigId = createConfig(nameSubstring = "alice-config", configType = ConfigType.SLACK, client = aliceClient!!)
        val bobConfigId = createConfig(nameSubstring = "bob-config", configType = ConfigType.SLACK, client = bobClient!!)

        // Alice should only see her own config
        val aliceList = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        val aliceConfigIds = extractConfigIds(aliceList)
        Assert.assertTrue("Alice should see her own config", aliceConfigIds.contains(aliceConfigId))
        Assert.assertFalse("Alice should NOT see Bob's config", aliceConfigIds.contains(bobConfigId))

        // Bob should only see his own config
        val bobList = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
        val bobConfigIds = extractConfigIds(bobList)
        Assert.assertTrue("Bob should see his own config", bobConfigIds.contains(bobConfigId))
        Assert.assertFalse("Bob should NOT see Alice's config", bobConfigIds.contains(aliceConfigId))

        // Share Alice's config with Bob
        shareResource(aliceClient!!, aliceConfigId, "notifications_read_only", bobUser)
        Thread.sleep(2000)

        // Now Bob should see both
        val bobListAfterShare = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
        val bobConfigIdsAfter = extractConfigIds(bobListAfterShare)
        Assert.assertTrue("Bob should see his own config", bobConfigIdsAfter.contains(bobConfigId))
        Assert.assertTrue("Bob should see shared config from Alice", bobConfigIdsAfter.contains(aliceConfigId))
    }

    // --- read_only access level ---

    fun `test read_only access grants only read`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)
        shareResource(aliceClient!!, configId, "notifications_read_only", bobUser)
        Thread.sleep(2000)

        // Bob can read
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
        Assert.assertNotNull(response)

        // Bob cannot update
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            buildUpdateJson("read_only user update attempt"),
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )

        // Verify config is unchanged after failed update attempt
        val afterFailedUpdate = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        val configName = afterFailedUpdate.get("config_list").asJsonArray[0].asJsonObject
            .get("config").asJsonObject.get("name").asString
        Assert.assertNotEquals("read_only user update attempt", configName)

        // Bob cannot delete
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )

        // Bob cannot share further
        val shareRequest = Request("PUT", "/_plugins/_security/api/resource/share")
        shareRequest.setJsonEntity(buildShareJson(configId, "notifications_read_only", charlieUser))
        executeRequest(shareRequest, RestStatus.FORBIDDEN.status, bobClient!!)
    }

    // --- read_write access level ---

    fun `test read_write access grants read update and delete but not share`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)
        shareResource(aliceClient!!, configId, "notifications_read_write", bobUser)
        Thread.sleep(2000)

        // Bob can read
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )

        // Bob can update
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            buildUpdateJson("updated by bob"),
            RestStatus.OK.status,
            bobClient!!
        )

        // Alice sees Bob's update
        val aliceGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        val configName = aliceGetResponse.get("config_list").asJsonArray[0].asJsonObject
            .get("config").asJsonObject.get("name").asString
        Assert.assertEquals("updated by bob", configName)

        // Bob cannot share — read_write does not include share action
        val shareRequest = Request("PUT", "/_plugins/_security/api/resource/share")
        shareRequest.setJsonEntity(buildShareJson(configId, "notifications_read_only", charlieUser))
        executeRequest(shareRequest, RestStatus.FORBIDDEN.status, bobClient!!)

        // Bob can delete
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
    }

    // --- full_access level ---

    fun `test full_access grants all operations including share`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)
        shareResource(aliceClient!!, configId, "notifications_full_access", bobUser)
        Thread.sleep(2000)

        // Bob can read
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )

        // Bob can update
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            buildUpdateJson("full access update by bob"),
            RestStatus.OK.status,
            bobClient!!
        )

        // Alice sees Bob's update
        val aliceGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        val updatedName = aliceGetResponse.get("config_list").asJsonArray[0].asJsonObject
            .get("config").asJsonObject.get("name").asString
        Assert.assertEquals("full access update by bob", updatedName)

        // Bob can share further with charlie at read_only level
        shareResource(bobClient!!, configId, "notifications_read_only", charlieUser)
        Thread.sleep(2000)

        // Charlie can read (shared by bob, not the owner)
        val charlieResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            charlieClient!!
        )
        Assert.assertNotNull(charlieResponse)

        // Charlie cannot delete (read_only access)
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            charlieClient!!
        )

        // Bob can delete (full_access)
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
    }

    // --- Revoke access ---

    fun `test revoking access removes permissions`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)
        shareResource(aliceClient!!, configId, "notifications_read_only", bobUser)
        Thread.sleep(2000)

        // Bob can read while shared
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )

        // Alice revokes Bob's access via PATCH with "revoke" field
        val revokeRequest = Request("PATCH", "/_plugins/_security/api/resource/share")
        revokeRequest.setJsonEntity(
            """
            {
              "resource_id": "$configId",
              "resource_type": "notification_config",
              "revoke": {
                "notifications_read_only": {
                    "users": ["$bobUser"]
                }
              }
            }
            """.trimIndent()
        )
        val revokeResponse = aliceClient!!.performRequest(revokeRequest)
        Assert.assertEquals(200, revokeResponse.statusLine.statusCode)
        Thread.sleep(2000)

        // Bob can no longer access
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            bobClient!!
        )
    }

    // --- Helpers ---

    private fun extractConfigIds(response: com.google.gson.JsonObject): List<String> {
        val ids = mutableListOf<String>()
        if (response.has("config_list")) {
            response.getAsJsonArray("config_list").forEach { item ->
                ids.add(item.asJsonObject.get("config_id").asString)
            }
        }
        return ids
    }

    private fun buildUpdateJson(name: String): String {
        return """
        {
            "config":{
                "name":"$name",
                "description":"updated config",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{"url":"https://hooks.slack.com/services/updated_url"}
            }
        }
        """.trimIndent()
    }

    private fun buildShareJson(resourceId: String, accessLevel: String, user: String): String {
        return """
        {
          "resource_id": "$resourceId",
          "resource_type": "notification_config",
          "share_with": {
            "$accessLevel": {
                "users": ["$user"]
            }
          }
        }
        """.trimIndent()
    }

    private fun shareResource(client: RestClient, resourceId: String, accessLevel: String, user: String) {
        val request = Request("PUT", "/_plugins/_security/api/resource/share")
        request.setJsonEntity(buildShareJson(resourceId, accessLevel, user))
        val response = client.performRequest(request)
        Assert.assertEquals(200, response.statusLine.statusCode)
    }

    private fun createNotificationsRole() {
        val request = Request("PUT", "/_plugins/_security/api/roles/$notificationsFullAccessRole")
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
    }
}
