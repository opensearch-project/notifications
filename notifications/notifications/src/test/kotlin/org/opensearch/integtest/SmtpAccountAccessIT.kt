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
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.getJsonString
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class SmtpAccountAccessIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
            org.junit.Assume.assumeFalse(System.getProperty("resource_sharing.enabled", "false")!!.toBoolean())
        }
    }

    private val user = "smtpSenderIntegTestUser"
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

    fun `test create smtp sender config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()
        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // Get SMTP account config
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

    fun `test get smtp sender has access with filter by backend roles enabled`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                getUserClient
            )
            verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get smtp sender does not have access with filter by backend roles enabled`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                getUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get smtp sender has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                getUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get smtp sender has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            createUserWithCustomRole(getUser, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf("role2", "role1", "role3"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                getUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test update smtp sender has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.SMTP_ACCOUNT,
                isEnabled = true,
                configData = sampleSmtpAccount
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"smtp_account",
                    "is_enabled":${referenceObject.isEnabled},
                    "smtp_account":$sampleSmtpJsonString
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.OK.status,
                updateUserClient
            )

            // Get SMTP account config
            val getConfigResponse = executeRequest(
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

    fun `test update smtp sender does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        // verifySingleConfigEquals(configId, referenceObject, getConfigResponse)

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does not contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.SMTP_ACCOUNT,
                isEnabled = true,
                configData = sampleSmtpAccount
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"smtp_account",
                    "is_enabled":${referenceObject.isEnabled},
                    "smtp_account":$sampleSmtpJsonString
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.FORBIDDEN.status,
                updateUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test update smtp sender has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.SMTP_ACCOUNT,
                isEnabled = true,
                configData = sampleSmtpAccount
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"smtp_account",
                    "is_enabled":${referenceObject.isEnabled},
                    "smtp_account":$sampleSmtpJsonString
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.OK.status,
                updateUserClient
            )

            // Get SMTP account config
            val getConfigResponse = executeRequest(
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

    fun `test update smtp sender does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does match all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.SMTP_ACCOUNT,
                isEnabled = true,
                configData = sampleSmtpAccount
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"smtp_account",
                    "is_enabled":${referenceObject.isEnabled},
                    "smtp_account":$sampleSmtpJsonString
                }
            }
            """.trimIndent()
            executeRequest(
                RestRequest.Method.PUT.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                updateRequestJsonString,
                RestStatus.FORBIDDEN.status,
                updateUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test delete smtp sender has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user contain all roles from create user
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                deleteUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete smtp sender does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does not contain all roles from create user
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                deleteUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete smtp sender has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user matches all roles from create user
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status,
                deleteUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }

    fun `test delete smtp sender does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])
        // Create sample config request reference
        val sampleSmtpAccount = SmtpAccount("example-host", 2465, MethodType.SSL, "no-reply@fake-host.com")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )
        val sampleSmtpJsonString = getJsonString(sampleSmtpAccount)

        // Create SMTP account config
        val createRequestJsonString = """
        {
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"smtp_account",
                "is_enabled":${referenceObject.isEnabled},
                "smtp_account":$sampleSmtpJsonString
            }
        }
        """.trimIndent()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does not match all roles from create user
            createUserWithCustomRole(deleteUser, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

            executeRequest(
                RestRequest.Method.DELETE.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.FORBIDDEN.status,
                deleteUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(deleteUser, NOTIFICATION_DELETE_CONFIG_ACCESS)
            deleteUserClient?.close()
        }
    }
}
