/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
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

package org.opensearch.notifications.resthandler

import org.junit.Assert
import org.opensearch.common.settings.SecureString
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.PluginRestTestCase
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import java.net.URLEncoder
import java.util.EnumSet

class CreateNotificationConfigIT : PluginRestTestCase() {

    fun `test Create slack notification config`() {
        // Create sample config request reference
        val sampleSlack = Slack("https://domain.com/sample_slack_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.Slack,
            EnumSet.of(Feature.IndexManagement, Feature.Reports),
            isEnabled = true,
            slack = sampleSlack
        )

        // Create slack notification config
        val createRequestJsonString = """
        {
            "notification_config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"Slack",
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "slack":{"url":"${referenceObject.slack!!.url}"}
            }
        }
        """.trimIndent()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get slack notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
    }

    fun `test Create chime notification config with ID`() {
        // Create sample config request reference
        val configId = "sample_config_id"
        val sampleChime = Chime("https://domain.com/sample_chime_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.Chime,
            EnumSet.of(Feature.Alerting, Feature.Reports),
            isEnabled = true,
            chime = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config_id":"$configId",
            "notification_config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"Chime",
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${referenceObject.chime!!.url}"}
            }
        }
        """.trimIndent()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        Assert.assertEquals(configId, createResponse.get("config_id").asString)
        Thread.sleep(1000)

        // Get chime notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
    }

    fun `test Create webhook notification config with existing ID fails`() {
        // Create sample config request reference
        val sampleWebhook = Webhook("https://domain.com/sample_webhook_url#1234567890")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement, Feature.Reports, Feature.Alerting),
            isEnabled = true,
            webhook = sampleWebhook
        )

        // Create webhook notification config
        val createRequestJsonString = """
        {
            "notification_config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"Webhook",
                "features":[
                    "${referenceObject.features.elementAt(0)}",
                    "${referenceObject.features.elementAt(1)}",
                    "${referenceObject.features.elementAt(2)}"
                ],
                "is_enabled":${referenceObject.isEnabled},
                "webhook":{"url":"${referenceObject.webhook!!.url}"}
            }
        }
        """.trimIndent()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(1000)

        // Get webhook notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, referenceObject, getConfigResponse)
        Thread.sleep(100)

        // Updated notification config object
        val anotherWebhook = Webhook("https://another.domain.com/another_webhook_url#0987654321")
        val anotherObject = NotificationConfig(
            "this is another config name",
            "this is another config description",
            ConfigType.Webhook,
            EnumSet.of(Feature.IndexManagement, Feature.Reports),
            isEnabled = true,
            webhook = anotherWebhook
        )

        // create another webhook notification config with same id
        val anotherRequestJsonString = """
        {
            "config_id":"$configId",
            "notification_config":{
                "name":"${anotherObject.name}",
                "description":"${anotherObject.description}",
                "config_type":"Webhook",
                "features":[
                    "${anotherObject.features.elementAt(0)}",
                    "${anotherObject.features.elementAt(1)}"
                ],
                "is_enabled":${anotherObject.isEnabled},
                "webhook":{"url":"${anotherObject.webhook!!.url}"}
            }
        }
        """.trimIndent()

        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            anotherRequestJsonString,
            RestStatus.CONFLICT.status
        )
    }

    fun `test Create smtpAccount notification config with ID containing special chars`() {
        // Create sample smtp account config request reference
        val configId = "~!@#$%^&*()_+`-=;',./<>?"
        val sampleSmtpAccount = SmtpAccount(
            "smtp.domain.com",
            1234,
            SmtpAccount.MethodType.StartTls,
            "from@domain.com",
            SecureString("username".toCharArray()),
            SecureString("password".toCharArray())
        )
        val smtpAccountConfig = NotificationConfig(
            "this is a sample smtp account config name",
            "this is a sample smtp account config description",
            ConfigType.SmtpAccount,
            EnumSet.of(Feature.Reports),
            isEnabled = true,
            smtpAccount = sampleSmtpAccount
        )

        // Create smtp account notification config
        val createSmtpAccountRequestJsonString = """
        {
            "config_id":"$configId",
            "notification_config":{
                "name":"${smtpAccountConfig.name}",
                "description":"${smtpAccountConfig.description}",
                "config_type":"SmtpAccount",
                "features":[
                    "${smtpAccountConfig.features.elementAt(0)}"
                ],
                "is_enabled":${smtpAccountConfig.isEnabled},
                "smtp_account":{
                    "host":"${sampleSmtpAccount.host}",
                    "port":"${sampleSmtpAccount.port}",
                    "method":"${sampleSmtpAccount.method}",
                    "from_address":"${sampleSmtpAccount.fromAddress}",
                    "username":"${sampleSmtpAccount.username!!.chars}",
                    "password":"${sampleSmtpAccount.password!!.chars}"
                }
            }
        }
        """.trimIndent()
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createSmtpAccountRequestJsonString,
            RestStatus.OK.status
        )
        Assert.assertEquals(configId, createResponse.get("config_id").asString)
        Thread.sleep(100)

        // Get email notification config
        val getSmtpAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/${URLEncoder.encode(configId, "utf-8")}",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(configId, smtpAccountConfig, getSmtpAccountResponse)
    }
}
