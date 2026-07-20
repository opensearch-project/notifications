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
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipient
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.getJsonString
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest

class EmailGroupAccessIT : PluginRestTestCase() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // things to execute once and keep around for the class
            org.junit.Assume.assumeTrue(System.getProperty("https", "false")!!.toBoolean())
        }
    }

    private val user = "emailGroupIntegTestUser"
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

    fun `test create email group config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // Get SMTP account config
            val getConfigResponse = executeRequest(
                RestRequest.Method.GET.name,
                "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
                "",
                RestStatus.OK.status
            )
            verifySingleConfigEquals(configId, emailGroupConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
        }
    }

    fun `test get email group has access with filter by backend roles enabled`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()
        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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
            verifySingleConfigEquals(configId, emailGroupConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get email group does not have access with filter by backend roles enabled`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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

    fun `test get email group has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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
            verifySingleConfigEquals(configId, emailGroupConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test get email group has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val getUser = "getUser"
        val getUserClient = buildUserClient(getUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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
            verifySingleConfigEquals(configId, emailGroupConfig, getConfigResponse)
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(getUser, NOTIFICATION_GET_CONFIG_ACCESS)
            getUserClient?.close()
        }
    }

    fun `test update email group has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )
        val sampleSmtpJsonString = getJsonString(emailGroupConfig)

        // Create email group notification config
        val createEmailGroupRequestJsonString = """
        {
            "config":{
                "name":"${emailGroupConfig.name}",
                "description":"${emailGroupConfig.description}",
                "config_type":"email_group",
                "is_enabled":${emailGroupConfig.isEnabled},
                "email_group":{
                    "recipient_list":[
                        {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                        {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
                    ]
                }
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL_GROUP,
                isEnabled = true,
                configData = sampleEmailGroup
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email_group",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email_group":{
                        "recipient_list":[
                            {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                            {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
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

    fun `test update email group does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )
        val sampleSmtpJsonString = getJsonString(emailGroupConfig)

        // Create email group notification config
        val createEmailGroupRequestJsonString = """
        {
            "config":{
                "name":"${emailGroupConfig.name}",
                "description":"${emailGroupConfig.description}",
                "config_type":"email_group",
                "is_enabled":${emailGroupConfig.isEnabled},
                "email_group":{
                    "recipient_list":[
                        {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                        {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
                    ]
                }
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does not contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL_GROUP,
                isEnabled = true,
                configData = sampleEmailGroup
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email_group",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email_group":{
                        "recipient_list":[
                            {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                            {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
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
                updateUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test update email group has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )
        val sampleSmtpJsonString = getJsonString(emailGroupConfig)

        // Create email group notification config
        val createEmailGroupRequestJsonString = """
        {
            "config":{
                "name":"${emailGroupConfig.name}",
                "description":"${emailGroupConfig.description}",
                "config_type":"email_group",
                "is_enabled":${emailGroupConfig.isEnabled},
                "email_group":{
                    "recipient_list":[
                        {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                        {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
                    ]
                }
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user contain all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL_GROUP,
                isEnabled = true,
                configData = sampleEmailGroup
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email_group",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email_group":{
                        "recipient_list":[
                            {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                            {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
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

    fun `test update email group does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        // Create sample config request reference
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )
        val sampleSmtpJsonString = getJsonString(emailGroupConfig)

        // Create email group notification config
        val createEmailGroupRequestJsonString = """
        {
            "config":{
                "name":"${emailGroupConfig.name}",
                "description":"${emailGroupConfig.description}",
                "config_type":"email_group",
                "is_enabled":${emailGroupConfig.isEnabled},
                "email_group":{
                    "recipient_list":[
                        {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                        {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
                    ]
                }
            }
        }
        """.trimIndent()

        val updateUser = "updateUser"
        val updateUserClient = buildUserClient(updateUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)

            // roles on update user does match all roles from create user
            createUserWithCustomRole(updateUser, password, NOTIFICATION_UPDATE_CONFIG_ACCESS, arrayOf("role1"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_UPDATE_CONFIG_ACCESS])

            val referenceObjectUpdate = NotificationConfig(
                "this is a sample config name updated",
                "this is a sample config description updated",
                ConfigType.EMAIL_GROUP,
                isEnabled = true,
                configData = sampleEmailGroup
            )
            val updateRequestJsonString = """
            {
                "config":{
                    "name":"${referenceObjectUpdate.name}",
                    "description":"${referenceObjectUpdate.description}",
                    "config_type":"email_group",
                    "is_enabled":${referenceObjectUpdate.isEnabled},
                    "email_group":{
                        "recipient_list":[
                            {"recipient":"${sampleEmailGroup.recipients[0].recipient}"},
                            {"recipient":"${sampleEmailGroup.recipients[1].recipient}"}
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
                updateUserClient
            )
        } finally {
            deleteUserWithCustomRole(user, NOTIFICATION_CREATE_CONFIG_ACCESS)
            deleteUserWithCustomRole(updateUser, NOTIFICATION_UPDATE_CONFIG_ACCESS)
            updateUserClient?.close()
        }
    }

    fun `test delete email group has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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

    fun `test delete email group does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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

    fun `test delete email group has access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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

    fun `test delete email group does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailGroupConfig, createEmailGroupRequestJsonString) = createTestEmailGroup()

        val deleteUser = "deleteUser"
        val deleteUserClient = buildUserClient(deleteUser, password)

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
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
