/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipient
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SesAccount
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifyMultiConfigEquals
import org.opensearch.notifications.verifyMultiConfigIdEquals
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.notifications.verifySingleConfigIdEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class EmailNotificationConfigCrudIT : PluginRestTestCase() {

    fun `test Create, Get, Update, Delete smtp email notification config using REST client`() {
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
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${smtpAccountConfig.features.elementAt(0)}"
                ],
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
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)

        // Create sample email group config request reference
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailGroupConfig.features.elementAt(0)}"
                ],
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
        val createEmailGroupResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailGroupRequestJsonString,
            RestStatus.OK.status
        )
        val emailGroupConfigId = createEmailGroupResponse.get("config_id").asString
        Assert.assertNotNull(emailGroupConfigId)
        Thread.sleep(100)

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
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}"
                ],
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
        val createEmailResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.OK.status
        )
        val emailConfigId = createEmailResponse.get("config_id").asString
        Assert.assertNotNull(emailConfigId)
        Thread.sleep(1000)

        // Get email notification config
        val getSmtpAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$smtpAccountConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(smtpAccountConfigId, smtpAccountConfig, getSmtpAccountResponse)
        Thread.sleep(100)

        val getEmailGroupResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailGroupConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(emailGroupConfigId, emailGroupConfig, getEmailGroupResponse)
        Thread.sleep(100)

        val getEmailResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(emailConfigId, emailConfig, getEmailResponse)
        Thread.sleep(100)

        // Get all notification config

        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(
            mapOf(
                Pair(smtpAccountConfigId, smtpAccountConfig),
                Pair(emailGroupConfigId, emailGroupConfig),
                Pair(emailConfigId, emailConfig)
            ),
            getAllConfigResponse
        )
        Thread.sleep(100)

        // Updated smtp account config object
        val updatedSmtpAccount = SmtpAccount(
            "updated.domain.com",
            4321,
            MethodType.SSL,
            "updated-from@domain.com"
        )
        val updatedSmtpAccountConfig = NotificationConfig(
            "this is a updated smtp account config name",
            "this is a updated smtp account config description",
            ConfigType.SMTP_ACCOUNT,
            setOf(FEATURE_REPORTS),
            isEnabled = true,
            configData = updatedSmtpAccount
        )

        // Update smtp account notification config
        val updateSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"${updatedSmtpAccountConfig.name}",
                "description":"${updatedSmtpAccountConfig.description}",
                "config_type":"smtp_account",
                "feature_list":[
                    "${updatedSmtpAccountConfig.features.elementAt(0)}"
                ],
                "is_enabled":${updatedSmtpAccountConfig.isEnabled},
                "smtp_account":{
                    "host":"${updatedSmtpAccount.host}",
                    "port":"${updatedSmtpAccount.port}",
                    "method":"${updatedSmtpAccount.method}",
                    "from_address":"${updatedSmtpAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()

        val updateSmtpAccountResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$smtpAccountConfigId",
            updateSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        Assert.assertEquals(smtpAccountConfigId, updateSmtpAccountResponse.get("config_id").asString)

        Thread.sleep(1000)

        // Get updated smtp account config

        val getUpdatedSmtpAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$smtpAccountConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(smtpAccountConfigId, updatedSmtpAccountConfig, getUpdatedSmtpAccountResponse)
        Thread.sleep(100)

        // Get all updated config
        val getAllUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(
            mapOf(
                Pair(smtpAccountConfigId, updatedSmtpAccountConfig),
                Pair(emailGroupConfigId, emailGroupConfig),
                Pair(emailConfigId, emailConfig)
            ),
            getAllUpdatedConfigResponse
        )
        Thread.sleep(100)

        // Delete email notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(emailConfigId).asString)
        Thread.sleep(1000)

        // Get email notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Create, Get, Update, Delete ses email notification config using REST client`() {
        // Create sample ses account config request reference
        val sampleSesAccount = SesAccount(
            "us-east-1",
            "arn:aws:iam::012345678912:role/iam-test",
            "from@domain.com"
        )
        val sesAccountConfig = NotificationConfig(
            "this is a sample ses account config name",
            "this is a sample ses account config description",
            ConfigType.SES_ACCOUNT,
            setOf(FEATURE_REPORTS),
            isEnabled = true,
            configData = sampleSesAccount
        )

        // Create ses account notification config
        val createSesAccountRequestJsonString = """
        {
            "config":{
                "name":"${sesAccountConfig.name}",
                "description":"${sesAccountConfig.description}",
                "config_type":"ses_account",
                "feature_list":[
                    "${sesAccountConfig.features.elementAt(0)}"
                ],
                "is_enabled":${sesAccountConfig.isEnabled},
                "ses_account":{
                    "region":"${sampleSesAccount.awsRegion}",
                    "role_arn":"${sampleSesAccount.roleArn}",
                    "from_address":"${sampleSesAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()
        val createSesAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSesAccountRequestJsonString,
            RestStatus.OK.status
        )
        val sesAccountConfigId = createSesAccountResponse.get("config_id").asString
        Assert.assertNotNull(sesAccountConfigId)
        Thread.sleep(100)

        // Create sample email group config request reference
        val sampleEmailGroup = EmailGroup(listOf(EmailRecipient("email1@email.com"), EmailRecipient("email2@email.com")))
        val emailGroupConfig = NotificationConfig(
            "this is a sample email group config name",
            "this is a sample email group config description",
            ConfigType.EMAIL_GROUP,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailGroupConfig.features.elementAt(0)}"
                ],
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
        val createEmailGroupResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailGroupRequestJsonString,
            RestStatus.OK.status
        )
        val emailGroupConfigId = createEmailGroupResponse.get("config_id").asString
        Assert.assertNotNull(emailGroupConfigId)
        Thread.sleep(100)

        // Create sample email config request reference
        val sampleEmail = Email(
            sesAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf(emailGroupConfigId)
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}"
                ],
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
        val createEmailResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.OK.status
        )
        val emailConfigId = createEmailResponse.get("config_id").asString
        Assert.assertNotNull(emailConfigId)
        Thread.sleep(1000)

        // Get email notification config
        val getSesAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$sesAccountConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(sesAccountConfigId, sesAccountConfig, getSesAccountResponse)
        Thread.sleep(100)

        val getEmailGroupResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailGroupConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(emailGroupConfigId, emailGroupConfig, getEmailGroupResponse)
        Thread.sleep(100)

        val getEmailResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(emailConfigId, emailConfig, getEmailResponse)
        Thread.sleep(100)

        // Get all notification config

        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(
            mapOf(
                Pair(sesAccountConfigId, sesAccountConfig),
                Pair(emailGroupConfigId, emailGroupConfig),
                Pair(emailConfigId, emailConfig)
            ),
            getAllConfigResponse
        )
        Thread.sleep(100)

        // Updated ses account config object
        val updatedSesAccount = SesAccount(
            "us-west-2",
            "arn:aws:iam::012345678912:role/updated-role-test",
            "updated-from@domain.com"
        )
        val updatedSesAccountConfig = NotificationConfig(
            "this is a updated ses account config name",
            "this is a updated ses account config description",
            ConfigType.SES_ACCOUNT,
            setOf(FEATURE_REPORTS),
            isEnabled = true,
            configData = updatedSesAccount
        )

        // Update ses account notification config
        val updateSesAccountRequestJsonString = """
        {
            "config":{
                "name":"${updatedSesAccountConfig.name}",
                "description":"${updatedSesAccountConfig.description}",
                "config_type":"ses_account",
                "feature_list":[
                    "${updatedSesAccountConfig.features.elementAt(0)}"
                ],
                "is_enabled":${updatedSesAccountConfig.isEnabled},
                "ses_account":{
                    "region":"${updatedSesAccount.awsRegion}",
                    "role_arn":"${updatedSesAccount.roleArn}",
                    "from_address":"${updatedSesAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()

        val updateSesAccountResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/configs/$sesAccountConfigId",
            updateSesAccountRequestJsonString,
            RestStatus.OK.status
        )
        Assert.assertEquals(sesAccountConfigId, updateSesAccountResponse.get("config_id").asString)

        Thread.sleep(1000)

        // Get updated ses account config

        val getUpdatedSesAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$sesAccountConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(sesAccountConfigId, updatedSesAccountConfig, getUpdatedSesAccountResponse)
        Thread.sleep(100)

        // Get all updated config
        val getAllUpdatedConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(
            mapOf(
                Pair(sesAccountConfigId, updatedSesAccountConfig),
                Pair(emailGroupConfigId, emailGroupConfig),
                Pair(emailConfigId, emailConfig)
            ),
            getAllUpdatedConfigResponse
        )
        Thread.sleep(100)

        // Delete email notification config
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals("OK", deleteResponse.get("delete_response_list").asJsonObject.get(emailConfigId).asString)
        Thread.sleep(1000)

        // Get email notification config after delete

        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(100)
    }

    fun `test Create email notification config without email_group IDs`() {
        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"sample smtp account config name",
                "description":"sample smtp account config description",
                "config_type":"smtp_account",
                "feature_list":[
                    "reports"
                ],
                "is_enabled":true,
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"1234",
                    "method":"start_tls",
                    "from_address":"from@domain.com"
                }
            }
        }
        """.trimIndent()
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)

        // Create sample email config request reference
        val sampleEmail = Email(
            smtpAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf()
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}"
                ],
                "is_enabled":${emailConfig.isEnabled},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "recipient_list":[
                        {"recipient":"${sampleEmail.recipients[0].recipient}"},
                        {"recipient":"${sampleEmail.recipients[1].recipient}"}
                    ],
                    "email_group_id_list":[]
                }
            }
        }
        """.trimIndent()
        val createEmailResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.OK.status
        )
        val emailConfigId = createEmailResponse.get("config_id").asString
        Assert.assertNotNull(emailConfigId)
        Thread.sleep(1000)

        val getEmailResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$emailConfigId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(emailConfigId, emailConfig, getEmailResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config using invalid email account id should fail`() {
        // Create sample email config request reference
        val sampleEmail = Email(
            "InvalidSmtpAccountConfigId",
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf()
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}"
                ],
                "is_enabled":${emailConfig.isEnabled},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "recipient_list":[
                        {"recipient":"${sampleEmail.recipients[0].recipient}"},
                        {"recipient":"${sampleEmail.recipients[1].recipient}"}
                    ],
                    "email_group_id_list":[]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(1000)

        // Get all notification config should give empty result
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(mapOf(), getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config using invalid group ID should fail`() {
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
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${smtpAccountConfig.features.elementAt(0)}"
                ],
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
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)

        // Create sample email config request reference
        val sampleEmail = Email(
            smtpAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf("InvalidEmailGroupConfigId")
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}"
                ],
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
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.NOT_FOUND.status
        )
        Thread.sleep(1000)

        // Get all notification config should give only smtp account
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigEquals(mapOf(Pair(smtpAccountConfigId, smtpAccountConfig)), getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config using email account id with unavailable feature should fail`() {
        // Create smtp account notification config with reporting and alerting feature
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"sample smtp account config name",
                "description":"sample smtp account config description",
                "config_type":"smtp_account",
                "feature_list":[
                    "reports",
                    "alerting"
                ],
                "is_enabled":true,
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"1234",
                    "method":"start_tls",
                    "from_address":"from@domain.com"
                }
            }
        }
        """.trimIndent()
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)

        // Create sample email config request reference with reports and index_management
        val sampleEmail = Email(
            smtpAccountConfigId,
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf()
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS, FEATURE_INDEX_MANAGEMENT),
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
                "feature_list":[
                    "${emailConfig.features.elementAt(0)}",
                    "${emailConfig.features.elementAt(1)}"
                ],
                "is_enabled":${emailConfig.isEnabled},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "recipient_list":[
                        {"recipient":"${sampleEmail.recipients[0].recipient}"},
                        {"recipient":"${sampleEmail.recipients[1].recipient}"}
                    ],
                    "email_group_id_list":[]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.FORBIDDEN.status
        )
        Thread.sleep(1000)

        // Get all notification config should give only email group
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(smtpAccountConfigId, getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config wit email_group IDs put as email account id should fail`() {

        // Create email group notification config
        val createEmailGroupRequestJsonString = """
        {
            "config":{
                "name":"sample email group name",
                "description":"sample email group description",
                "config_type":"email_group",
                "feature_list":["reports"],
                "is_enabled":true,
                "email_group":{
                    "recipient_list":[ {"recipient":"email1@email.com"}, {"recipient":"email2@email.com"}]
                }
            }
        }
        """.trimIndent()
        val createEmailGroupResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailGroupRequestJsonString,
            RestStatus.OK.status
        )
        val emailGroupConfigId = createEmailGroupResponse.get("config_id").asString
        Assert.assertNotNull(emailGroupConfigId)
        Thread.sleep(100)

        // Create email notification config
        val createEmailRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"email",
                "feature_list":["reports"],
                "is_enabled":true,
                "email":{
                    "email_account_id":"$emailGroupConfigId",
                    "recipient_list":[],
                    "email_group_id_list":[]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.NOT_ACCEPTABLE.status
        )
        Thread.sleep(1000)

        // Get all notification config should give only email group
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(emailGroupConfigId, getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config with email account ID put as well email group id should fail`() {
        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"sample smtp account config name",
                "description":"sample smtp account config description",
                "config_type":"smtp_account",
                "feature_list":[
                    "reports"
                ],
                "is_enabled":true,
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"1234",
                    "method":"start_tls",
                    "from_address":"from@domain.com"
                }
            }
        }
        """.trimIndent()
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)
        val createEmailRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"email",
                "feature_list":["reports"],
                "is_enabled":true,
                "email":{
                    "email_account_id":"$smtpAccountConfigId",
                    "recipient_list":[],
                    "email_group_id_list":["$smtpAccountConfigId"]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.BAD_REQUEST.status
        )
        Thread.sleep(1000)

        // Get all notification config should give only email group
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(smtpAccountConfigId, getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Create email notification config with email account IDs put in email group id should fail`() {
        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config":{
                "name":"sample smtp account config name",
                "description":"sample smtp account config description",
                "config_type":"smtp_account",
                "feature_list":[
                    "reports"
                ],
                "is_enabled":true,
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"1234",
                    "method":"start_tls",
                    "from_address":"from@domain.com"
                }
            }
        }
        """.trimIndent()
        val createSmtpAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val smtpAccountConfigId = createSmtpAccountResponse.get("config_id").asString
        Assert.assertNotNull(smtpAccountConfigId)
        Thread.sleep(100)

        // Create another smtp account
        val anotherAccountResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        val anotherAccountConfigId = anotherAccountResponse.get("config_id").asString
        Assert.assertNotNull(anotherAccountConfigId)
        Thread.sleep(100)
        val createEmailRequestJsonString = """
        {
            "config":{
                "name":"this is a sample config name",
                "description":"this is a sample config description",
                "config_type":"email",
                "feature_list":["reports"],
                "is_enabled":true,
                "email":{
                    "email_account_id":"$smtpAccountConfigId",
                    "recipient_list":[],
                    "email_group_id_list":["$anotherAccountConfigId"]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.NOT_ACCEPTABLE.status
        )
        Thread.sleep(1000)

        // Get all notification config should give only email group
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(setOf(smtpAccountConfigId, anotherAccountConfigId), getAllConfigResponse)
        Thread.sleep(100)
    }

    fun `test Bad Request for multiple config for SmtpAccount using REST Client`() {
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
            setOf(FEATURE_REPORTS),
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
                "features":[
                    "${smtpAccountConfig.features.elementAt(0)}"
                ],
                "is_enabled":${smtpAccountConfig.isEnabled},
                "slack": {"url": "https://dummy.com"},
                "smtp_account":{
                    "host":"${sampleSmtpAccount.host}",
                    "port":"${sampleSmtpAccount.port}",
                    "method":"${sampleSmtpAccount.method}",
                    "from_address":"${sampleSmtpAccount.fromAddress}"
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test Bad Request for multiple config for Email using REST Client Email`() {
        // Create sample email config request reference
        val sampleEmail = Email(
            "dummy",
            listOf(EmailRecipient("default-email1@email.com"), EmailRecipient("default-email2@email.com")),
            listOf("dummy")
        )
        val emailConfig = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.EMAIL,
            setOf(FEATURE_REPORTS),
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
                "features":[
                    "${emailConfig.features.elementAt(0)}"
                ],
                "is_enabled":${emailConfig.isEnabled},
                "slack":{"url": "https://dummy.com"},
                "email":{
                    "email_account_id":"${sampleEmail.emailAccountID}",
                    "default_recipients":[
                        "{"recipient":${sampleEmail.recipients[0].recipient}}",
                        "{"recipient":${sampleEmail.recipients[1].recipient}}"
                    ],
                    "default_email_group_ids":[
                        "${sampleEmail.emailGroupIds[0]}"
                    ]
                }
            }
        }
        """.trimIndent()
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createEmailRequestJsonString,
            RestStatus.BAD_REQUEST.status
        )
    }
}
