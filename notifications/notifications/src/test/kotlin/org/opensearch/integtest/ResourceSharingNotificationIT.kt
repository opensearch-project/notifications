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
import org.opensearch.client.ResponseException
import org.opensearch.client.RestClient
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.rest.RestRequest

/**
 * Integration tests for Resource Sharing feature with Notifications plugin.
 * Only runs when both security and resource_sharing are enabled.
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

    private val aliceUser = "rs_alice"
    private val alicePassword = "TopSecret_1234%Alice"
    private val bobUser = "rs_bob"
    private val bobPassword = "TopSecret_1234%Bobby"
    private var aliceClient: RestClient? = null
    private var bobClient: RestClient? = null

    @Before
    fun setupUsers() {
        if (aliceClient != null) return
        createUser(aliceUser, alicePassword, arrayOf("engineering"))
        addPatchUserRolesMapping(ALL_ACCESS_ROLE, arrayOf(aliceUser))
        aliceClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), aliceUser, alicePassword)
            .setSocketTimeout(60000).build()

        createUser(bobUser, bobPassword, arrayOf("marketing"))
        addPatchUserRolesMapping(ALL_ACCESS_ROLE, arrayOf(bobUser))
        bobClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), bobUser, bobPassword)
            .setSocketTimeout(60000).build()
    }

    @After
    fun cleanupClients() {
        aliceClient?.close()
        bobClient?.close()
        aliceClient = null
        bobClient = null
    }

    fun `test config created by alice is not visible to bob`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        // Bob should not be able to get Alice's config
        val exception = Assert.assertThrows(ResponseException::class.java) {
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                0,
                bobClient!!
            )
        }
        Assert.assertTrue(
            exception.message!!.contains("no permissions") || exception.response.statusLine.statusCode == 403
        )
    }

    fun `test config created by alice is visible after sharing`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        // Share with bob
        shareResource(aliceClient!!, configId, "notification_config", "notifications_read_only", bobUser)
        Thread.sleep(2000)

        // Bob should now be able to get the config
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            200,
            bobClient!!
        )
        Assert.assertNotNull(response)
    }

    private fun shareResource(client: RestClient, resourceId: String, resourceType: String, accessLevel: String, user: String) {
        val request = Request("PUT", "/_plugins/_security/api/resource/share")
        request.setJsonEntity(
            """
            {
              "resource_id": "$resourceId",
              "resource_type": "$resourceType",
              "share_with": {
                "$accessLevel": {
                    "users": ["$user"]
                }
              }
            }
            """.trimIndent()
        )
        val response = client.performRequest(request)
        Assert.assertEquals(200, response.statusLine.statusCode)
    }
}
