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
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipient
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.rest.SecureRestClientBuilder
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
        userClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), user, password)
            .setSocketTimeout(60000)
            .setConnectionRequestTimeout(180000)
            .build()
    }

    @After
    fun cleanup() {
        userClient?.close()
        userClient = null
    }

    fun `test Create email notification config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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
        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

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

        // Create sample smtp account config request reference
        val sampleSmtpAccount = SmtpAccount(
            "smtp.domain.com",
            1234,
            MethodType.START_TLS,
            "from@domain.com"
        )
        val smtpAccountConfig = NotificationConfig(
            "this is a sample smtp account config name",
            "this is a sample smtp account config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"${smtpAccountConfig.name}",
                "description":"${smtpAccountConfig.description}",
                "config_type":"smtp_account",
                "is_enabled":${smtpAccountConfig.isEnabled},
                "smtp_account":{
                    "host":"${sampleSmtpAccount.host}",
                    "port":"${sampleSmtpAccount.port}",
                    "method":"${sampleSmtpAccount.method}",
                    "from_address":"${sampleSmtpAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()
        val smtpAccountConfigId = createConfigWithRequestJsonString(createSmtpAccountRequestJsonString)

        // Create sample email group config request reference
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )

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
        val emailGroupConfigId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString)

        // Create sample email config request reference
        val sampleEmail = Email(
            smtpAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf(emailGroupConfigId)
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            isEnabled = true,
            configData = sampleEmail
        )

        // Create email notification config
        val createEmailRequestJsonString = """
        {
            "config":{
                "name":"${emailConfig.name}",
                "description":"${emailConfig.description}",
                "config_type":"email",
                "is_enabled":${emailConfig.isEnabled},
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
        val configId = createConfigWithRequestJsonString(createEmailRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

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
        Thread.sleep(1000)

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

        // Create sample smtp account config request reference
        val sampleSmtpAccount = SmtpAccount(
            "smtp.domain.com",
            1234,
            MethodType.START_TLS,
            "from@domain.com"
        )
        val smtpAccountConfig = NotificationConfig(
            "this is a sample smtp account config name",
            "this is a sample smtp account config description",
            ConfigType.SMTP_ACCOUNT,
            isEnabled = true,
            configData = sampleSmtpAccount
        )

        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"${smtpAccountConfig.name}",
                "description":"${smtpAccountConfig.description}",
                "config_type":"smtp_account",
                "is_enabled":${smtpAccountConfig.isEnabled},
                "smtp_account":{
                    "host":"${sampleSmtpAccount.host}",
                    "port":"${sampleSmtpAccount.port}",
                    "method":"${sampleSmtpAccount.method}",
                    "from_address":"${sampleSmtpAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()
        val smtpAccountConfigId = createConfigWithRequestJsonString(createSmtpAccountRequestJsonString)

        // Create sample email group config request reference
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            isEnabled = true,
            configData = sampleEmailGroup
        )

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
        val emailGroupConfigId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString)

        // Create sample email config request reference
        val sampleEmail = Email(
            smtpAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf(emailGroupConfigId)
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            isEnabled = true,
            configData = sampleEmail
        )

        // Create email notification config
        val createEmailRequestJsonString = """
        {
            "config":{
                "name":"${emailConfig.name}",
                "description":"${emailConfig.description}",
                "config_type":"email",
                "is_enabled":${emailConfig.isEnabled},
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

        val configId = createConfigWithRequestJsonString(createEmailRequestJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

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
        deleteUserWithCustomRole(user, NOTIFICATION_UPDATE_CONFIG_ACCESS)
    }

    fun `test get email notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_GET_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_GET_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

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

    fun `test delete email notification config with user that has get Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_DELETE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_DELETE_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

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

    fun `test send test email message with send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_TEST_SEND_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_TEST_SEND_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
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

    fun `test send test email message without send permissions`() {
        createUserWithCustomRole(user, password, NOTIFICATION_NO_ACCESS_ROLE, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_NO_ACCESS_ROLE])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()
        val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString)
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

    fun `test send test email message has access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        val sendUser = "sendUser"
        val sendUserClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), sendUser, password)
            .setSocketTimeout(60000)
            .setConnectionRequestTimeout(180000)
            .build()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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

    fun `test send test email message does not have access when filter by backend access strategy is all`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.ALL.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        val sendUser = "sendUser"
        val sendUserClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), sendUser, password)
            .setSocketTimeout(60000)
            .setConnectionRequestTimeout(180000)
            .build()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        val sendUser = "sendUser"
        val sendUserClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), sendUser, password)
            .setSocketTimeout(60000)
            .setConnectionRequestTimeout(180000)
            .build()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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

    fun `test send test email message does not have access when filter by backend access strategy is exact`() {
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES.key, true))
        updateClusterSettings(ClusterSetting("persistent", PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY.key, FilterByBackendRolesAccessStrategy.EXACT.strategy))

        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf("role1", "role2"), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

        val (emailConfig, createEmailNotificationJsonString) = createTestEmailNotification()

        val sendUser = "sendUser"
        val sendUserClient = SecureRestClientBuilder(clusterHosts.toTypedArray(), isHttps(), sendUser, password)
            .setSocketTimeout(60000)
            .setConnectionRequestTimeout(180000)
            .build()

        try {
            val configId = createConfigWithRequestJsonString(createEmailNotificationJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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
