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
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.verifyChannelIdEquals
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class SecurityNotificationIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic fun setup() {
            // things to execute once and keep around for the class
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
        }
    }

    private val user = "integTestUser"
    private val password = "AeTq($%u-44c_j9NJB45a#2#JP7sH"
    var userClient: RestClient? = null

    @Before
    fun create() {

        if (userClient == null) {
            createUser(user, password, arrayOf())
            userClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), user, password).setSocketTimeout(60000).build()
        }
    }

    @After
    fun cleanup() {

        userClient?.close()
    }

    fun `test Create slack notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

            // Get Slack notification config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
        }
    }

    fun `test Create slack notification config without create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()

        executeRequest(
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            createRequestJsonString,
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test update slack notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get Slack notification config
        var getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)

        val referenceObjectUpdate = NotificationConfig(
            "this is a sample config name updated",
            "this is a sample config description updated",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${referenceObjectUpdate.name}",
                "description":"${referenceObjectUpdate.description}",
                "config_type":"slack",
                "is_enabled":${referenceObjectUpdate.isEnabled},
                "slack":{"url":"${(referenceObjectUpdate.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            updateRequestJsonString,
            RestStatus.OK.status,
            userClient!!
        )
        Thread.sleep(1000)

        // Get Slack notification config
        getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObjectUpdate, getConfigResponse)

        deleteUserWithCustomRole(user, NOTIFICATION_UPDATE_CONFIG_ACCESS)
    }

    fun `test update slack notification config without create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()

        executeRequest(
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            createRequestJsonString,
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test get slack notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CONFIG_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get Slack notification config
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            userClient!!
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        deleteUserWithCustomRole(user, NOTIFICATION_GET_CONFIG_ACCESS)
    }

    fun `test get slack notification config without get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Get Slack notification config

        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/randomConfig",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test delete slack notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_DELETE_CONFIG_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SLACK,
            isEnabled = true,
            configData = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"slack",
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${(referenceObject.configData as Slack).url}"}
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Delete Slack notification config
        deleteConfig(configId, userClient!!)

        // Should not be able to find config
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.NOT_FOUND.status
        )

        deleteUserWithCustomRole(user, NOTIFICATION_DELETE_CONFIG_ACCESS)
    }

    fun `test delete slack notification config without get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Get Slack notification config

        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/randomConfig",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test getChannelList should return only channels with get channel permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CHANNEL_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CHANNEL_ACCESS])

        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val emailId = createConfig(
            configType = ConfigType.EMAIL,
            smtpAccountId = smtpAccountId,
            emailGroupId = setOf(emailGroupId)
        )
        Thread.sleep(1000)

        val channelIds = setOf(slackId, chimeId, webhookId, emailId)
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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

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
        createUserWithCustomRole(user, password, NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_PLUGIN_FEATURE_ACCESS])

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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/features",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test send test slack message with send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_TEST_SEND_ACCESS, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{
                    "url":"https://hooks.slack.com/services/xxx/xxx"
                }
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // send test message
        val sendResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
            "",
            RestStatus.INTERNAL_SERVER_ERROR.status,
            userClient!!
        )

        // verify failure response is with message
        val error = sendResponse.get("error").asJsonObject
        Assert.assertNotNull(error.get("reason").asString)
        Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"500\""))

        deleteUserWithCustomRole(user, NOTIFICATION_TEST_SEND_ACCESS)
    }

    fun `test send test slack message without send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, "", ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Create webhook notification config
        val createRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{
                    "url":"https://hooks.slack.com/services/xxx/xxx"
                }
            }
        }
        """.trimIndent()
        val configId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // send test message
        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )

        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }
}
