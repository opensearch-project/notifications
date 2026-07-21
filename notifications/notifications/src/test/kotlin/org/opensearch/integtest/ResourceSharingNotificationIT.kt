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
    private var aliceClient: RestClient? = null
    private var bobClient: RestClient? = null

    @Before
    fun setupUsers() {
        if (aliceClient != null) return
        createCustomRole(notificationsFullAccessRole, "cluster:admin/opensearch/notifications/*")
        createUser(aliceUser, alicePassword, arrayOf("engineering"))
        createUser(bobUser, bobPassword, arrayOf("marketing"))
        createUserRolesMapping(notificationsFullAccessRole, arrayOf(aliceUser, bobUser))

        aliceClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), aliceUser, alicePassword)
            .setSocketTimeout(60000).build()
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

    fun `test create and get notification config with resource sharing enabled`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        val response = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            aliceClient!!
        )
        Assert.assertNotNull(response)
    }

    fun `test share resource with another user`() {
        val configId = createConfig(configType = ConfigType.SLACK, client = aliceClient!!)

        // Share with bob
        val shareRequest = Request("PUT", "/_plugins/_security/api/resource/share")
        shareRequest.setJsonEntity(
            """
            {
              "resource_id": "$configId",
              "resource_type": "notification_config",
              "share_with": {
                "notifications_read_only": {
                    "users": ["$bobUser"]
                }
              }
            }
            """.trimIndent()
        )
        val shareResponse = aliceClient!!.performRequest(shareRequest)
        Assert.assertEquals(200, shareResponse.statusLine.statusCode)

        Thread.sleep(2000)

        // Bob should now be able to get the config
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            bobClient!!
        )
        Assert.assertNotNull(response)
    }
}
