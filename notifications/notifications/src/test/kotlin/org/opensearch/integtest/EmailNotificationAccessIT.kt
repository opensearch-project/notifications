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
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class EmailNotificationAccessIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // things to execute once and keep around for the class
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
        }
    }

    private val user = "emailNotificationTestUser"
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

    fun `test Create email notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // Get email notification config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
        }
    }

    fun `test Create email notification config without create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        executeRequest(
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            createEmailNotificationJsonString,
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test update email notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)

        // Get email notification config
        var getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

        val referenceObjectUpdate = NotificationConfig(
            "this is a sample config name updated",
            "this is a sample config description updated",
            ConfigType.EMAIL,
            isEnabled = true,
            configData = sampleEmail
        )
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${referenceObjectUpdate.name}",
                "description":"${referenceObjectUpdate.description}",
                "config_type":"email",
                "is_enabled":${referenceObjectUpdate.isEnabled},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "recipient_list":[
                        {"recipient":"${sampleEmail.recipients[0].recipient}"},
                        {"recipient":"${sampleEmail.recipients[1].recipient}"}
                    ],
                    "email_group_id_list":[
                        "${sampleEmail.emailGroupIds[0]}"
                    ]
                }
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

        // Get email notification config
        getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObjectUpdate, getConfigResponse)

        deleteUserWithCustomRole(user, NOTIFICATION_UPDATE_CONFIG_ACCESS)
    }

    fun `test update email notification config without create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)

        // Get email notification config
        var getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

        val referenceObjectUpdate = NotificationConfig(
            "this is a sample config name updated",
            "this is a sample config description updated",
            ConfigType.EMAIL,
            isEnabled = true,
            configData = sampleEmail
        )
        val updateRequestJsonString = """
        {
            "config":{
                "name":"${referenceObjectUpdate.name}",
                "description":"${referenceObjectUpdate.description}",
                "config_type":"email",
                "is_enabled":${referenceObjectUpdate.isEnabled},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "recipient_list":[
                        {"recipient":"${sampleEmail.recipients[0].recipient}"},
                        {"recipient":"${sampleEmail.recipients[1].recipient}"}
                    ],
                    "email_group_id_list":[
                        "${sampleEmail.emailGroupIds[0]}"
                    ]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.PUT.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            updateRequestJsonString,
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test update email notification config has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            // Get email notification config
            var getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL,
                isEnabled = true,
                configData = sampleEmail
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email":{
                        "email_account_id":"${sampleEmail.emailAccountID}",
                        "recipient_list":[
                            {"recipient":"${sampleEmail.recipients[0].recipient}"},
                            {"recipient":"${sampleEmail.recipients[1].recipient}"}
                        ],
                        "email_group_id_list":[
                            "${sampleEmail.emailGroupIds[0]}"
                        ]
                    }
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.OK.status,
                updateUserClient!!
            )

            // Get email notification config
            getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, referenceObjectUpdate, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test update email notification config does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            // Get email notification config
            var getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL,
                isEnabled = true,
                configData = sampleEmail
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email":{
                        "email_account_id":"${sampleEmail.emailAccountID}",
                        "recipient_list":[
                            {"recipient":"${sampleEmail.recipients[0].recipient}"},
                            {"recipient":"${sampleEmail.recipients[1].recipient}"}
                        ],
                        "email_group_id_list":[
                            "${sampleEmail.emailGroupIds[0]}"
                        ]
                    }
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.FORBIDDEN.status,
                updateUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test update email notification config has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            // Get email notification config
            var getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL,
                isEnabled = true,
                configData = sampleEmail
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email":{
                        "email_account_id":"${sampleEmail.emailAccountID}",
                        "recipient_list":[
                            {"recipient":"${sampleEmail.recipients[0].recipient}"},
                            {"recipient":"${sampleEmail.recipients[1].recipient}"}
                        ],
                        "email_group_id_list":[
                            "${sampleEmail.emailGroupIds[0]}"
                        ]
                    }
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.OK.status,
                updateUserClient!!
            )

            // Get email notification config
            getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, referenceObjectUpdate, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test update email notification config does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            // Get email notification config
            var getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)

            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2", "role3"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL,
                isEnabled = true,
                configData = sampleEmail
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email":{
                        "email_account_id":"${sampleEmail.emailAccountID}",
                        "recipient_list":[
                            {"recipient":"${sampleEmail.recipients[0].recipient}"},
                            {"recipient":"${sampleEmail.recipients[1].recipient}"}
                        ],
                        "email_group_id_list":[
                            "${sampleEmail.emailGroupIds[0]}"
                        ]
                    }
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.FORBIDDEN.status,
                updateUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test get email notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)

        // Get email notification config
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            userClient!!
        )
        verifySingleConfigEquals(configId, emailConfig, getConfigResponse)
        deleteUserWithCustomRole(user, NOTIFICATION_GET_CONFIG_ACCESS)
    }

    fun `test get email notification config without get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/randomConfig",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test get email notification config does have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get email notification config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                getUserClient!!
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get email notification config does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get email notification config
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                getUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get email notification config does have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get email notification config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                getUserClient!!
            )
            verifySingleConfigEquals(configId, emailConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get email notification config does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1", "role2", "role3"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get email notification config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                getUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test delete email notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)

        // Delete email notification config
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

    fun `test delete email notification config without get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        // Get email notification config
        executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/randomConfig",
            "",
            RestStatus.FORBIDDEN.status,
            userClient!!
        )
        deleteUserWithCustomRole(user, NOTIFICATION_NO_ACCESS_ROLE)
    }

    fun `test delete email notification config has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            // Delete email notification config
            deleteConfig(configId, deleteUserClient!!)

            // Should not be able to find config
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.NOT_FOUND.status
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete email notification config does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                deleteUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete email notification config has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            // Delete email notification config
            deleteConfig(configId, deleteUserClient!!)

            // Should not be able to find config
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.NOT_FOUND.status
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete email notification config does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
        Assert.assertNotNull(configId)

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1", "role2", "role3"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                deleteUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test send test email message with send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)

        // send test message
        val sendResponse = executeRequest(
            RestRequest.Method.GET.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
            "",
            RestStatus.SERVICE_UNAVAILABLE.status,
            userClient!!
        )

        // verify failure response is with message
        val error = sendResponse.get("error").asJsonObject
        Assert.assertNotNull(error.get("reason").asString)
        Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"503\""))

        deleteUserWithCustomRole(user, NOTIFICATION_TEST_SEND_ACCESS)
    }

    fun `test send test email message without send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

            // send test message
            executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                sendUserClient!!
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_NO_ACCESS_ROLE)
            sendUserClient?.close()
        }
    }

    fun `test send test email message has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.SERVICE_UNAVAILABLE.status,
                sendUserClient!!
            )

            // verify failure response is with message
            val error = sendResponse.get("error").asJsonObject
            Assert.assertNotNull(error.get("reason").asString)
            Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"503\""))
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }

    fun `test send test email message does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
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

    fun `test send test email message has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(sendUser, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

            // send test message
            val sendResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/feature/test/$configId",
                "",
                RestStatus.SERVICE_UNAVAILABLE.status,
                sendUserClient!!
            )

            // verify failure response is with message
            val error = sendResponse.get("error").asJsonObject
            Assert.assertNotNull(error.get("reason").asString)
            Assert.assertTrue(error.get("reason").asString.contains("\"delivery_status\":{\"status_code\":\"503\""))
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(sendUser, NOTIFICATION_TEST_SEND_ACCESS)
            sendUserClient?.close()
        }
    }

    fun `test send test email message does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (sampleEmail, emailConfig, createEmailNotificationJsonString) = createTestEmailNotification(userClient!!)

        val sendUser = "sendUser"
        val sendUserClient = buildUserClient(sendUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
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
