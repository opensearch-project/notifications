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
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class SlackNotificationAccessIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            org.junit.Assume.assumeFalse(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
        }
    }

    private val user = "slackNotificationTestUser"
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

    fun `test Create slack notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

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
        createUserWithCustomRole(user, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

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

    fun `test send test slack message with send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

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
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

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

    fun `test send test slack message has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

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

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.INTERNAL_SERVER_ERROR.status,
                sendUserClient!!
            )

            // verify failure response is with message
            val error = sendResponse.get("error").asJsonObject
            Assert.assertNotNull(error.get("reason").asString)
            Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"500\""))
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }

    fun `test send test slack message does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

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

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                sendUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }

    fun `test send test slack message has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

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

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.INTERNAL_SERVER_ERROR.status,
                sendUserClient!!
            )

            // verify failure response is with message
            val error = sendResponse.get("error").asJsonObject
            Assert.assertNotNull(error.get("reason").asString)
            Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"500\""))
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }

    fun `test send test slack message does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

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

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                sendUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }
}
