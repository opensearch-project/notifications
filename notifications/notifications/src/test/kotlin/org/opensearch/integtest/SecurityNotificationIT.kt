/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.opensearch.client.RestClient
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.verifyChannelIdEquals
import org.opensearch.rest.RestRequest

class SecurityNotificationIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // things to execute once and keep around for the class
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            // Skip these tests when resource sharing is enabled - ResourceSharingNotificationIT covers that path
            org.junit.Assume.assumeFalse(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
        }
    }

    private val user = "integTestUser"
    private val password = randomAlphaOfLength(6) + "_" + randomIntBetween(1000, 10000) + "!" + randomAlphaOfLength(10)
    var userClient: RestClient? = null

    @Before
    fun create() {
        createUser(user, password, arrayOf())
        userClient = buildUserClient(user, password)
    }

    @After
    fun cleanup() {
        userClient?.close()
        userClient = null
    }

    fun `test getChannelList should return only channels with get channel permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CHANNEL_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CHANNEL_ACCESS])

        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val emailId = createConfig(
            configType = ConfigType.EMAIL,
            smtpAccountId = smtpAccountId,
            emailGroupId = setOf(emailGroupId)
        )
        Thread.sleep(1000)

        val channelIds = setOf(slackId, chimeId, microsoftTeamsId, webhookId, emailId)
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/channels",
            "",
            RestStatus.OK.status,
            userClient!!
        )
        Thread.sleep(100)
        verifyChannelIdEquals(channelIds, response, channelIds.size)

        deleteUserWithCustomRole(user, NOTIFICATION_GET_CHANNEL_ACCESS)
    }

    fun `test getChannelList fails without get channel permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        createConfig(configType = ConfigType.SLACK)
        Thread.sleep(1000)

        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/channels",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )

        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test Get plugin features should return non-empty configTypes with get features permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS])

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/features",
            "",
            RestStatus.OK.status,
            userClient!!
        )
        Assert.assertFalse(getResponse.get("allowed_config_type_list").asJsonArray.isEmpty)
        val pluginFeatures = getResponse.get("plugin_features").asJsonObject
        Assert.assertFalse(pluginFeatures.keySet().isEmpty())
        deleteUserWithCustomRole(user, NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS)
    }

    fun `test Get plugin features fails without get features permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/features",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }
}
