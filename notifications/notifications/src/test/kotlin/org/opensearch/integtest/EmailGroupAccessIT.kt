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
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.getJsonString
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

    fun `test create email group config with user that has create Notification permission`() {
        createUserWithCustomRole(user, password, NOTIFICATION_CREATE_CONFIG_ACCESS, arrayOf(""), ROLE_TO_PERMISSION_MAPPING[NOTIFICATION_CREATE_CONFIG_ACCESS])

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

        try {
            val configId = createConfigWithRequestJsonString(createEmailGroupRequestJsonString, userClient!!)
            Assert.assertNotNull(configId)
            Thread.sleep(1000)

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
}
