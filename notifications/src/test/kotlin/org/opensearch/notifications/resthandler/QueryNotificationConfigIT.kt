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
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.Feature.ALERTING
import org.opensearch.commons.notifications.model.Feature.INDEX_MANAGEMENT
import org.opensearch.commons.notifications.model.Feature.REPORTS
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.PluginRestTestCase
import org.opensearch.notifications.verifyMultiConfigIdEquals
import org.opensearch.notifications.verifyOrderedConfigList
import org.opensearch.notifications.verifySingleConfigIdEquals
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import kotlin.random.Random

class QueryNotificationConfigIT : PluginRestTestCase() {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun getCreateRequestJsonString(
        nameSubstring: String,
        configType: ConfigType,
        isEnabled: Boolean,
        features: Set<Feature>
    ): String {
        val randomString = (1..20)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
        val featuresString = features.joinToString { "\"$it\"" }
        val configObjectString = when (configType) {
            ConfigType.SLACK -> """
                "slack":{"url":"https://slack.domain.com/sample_slack_url#$randomString"}
            """.trimIndent()
            ConfigType.CHIME -> """
                "chime":{"url":"https://chime.domain.com/sample_chime_url#$randomString"}
            """.trimIndent()
            ConfigType.WEBHOOK -> """
                "webhook":{"url":"https://web.domain.com/sample_web_url#$randomString"}
            """.trimIndent()
            ConfigType.SMTP_ACCOUNT -> """
                "smtp_account":{
                    "host":"smtp.domain.com",
                    "port":"4321",
                    "method":"ssl",
                    "from_address":"$randomString@from.com"
                }
            """.trimIndent()
            ConfigType.EMAIL_GROUP -> """
                "email_group":{
                    "recipient_list":[
                        "$randomString+recipient1@from.com",
                        "$randomString+recipient2@from.com"
                    ]
                }
            """.trimIndent()
            else -> throw IllegalArgumentException("Unsupported configType=$configType")
        }
        return """
        {
            "config_id":"$randomString",
            "config":{
                "name":"$nameSubstring:this is a sample config name $randomString",
                "description":"this is a sample config description $randomString",
                "config_type":"$configType",
                "feature_list":[$featuresString],
                "is_enabled":$isEnabled,
                $configObjectString
            }
        }
        """.trimIndent()
    }

    private fun createConfig(
        nameSubstring: String = "",
        configType: ConfigType = ConfigType.SLACK,
        isEnabled: Boolean = true,
        features: Set<Feature> = setOf(ALERTING, INDEX_MANAGEMENT, Feature.REPORTS)
    ): String {
        val createRequestJsonString = getCreateRequestJsonString(nameSubstring, configType, isEnabled, features)
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/configs",
            createRequestJsonString,
            RestStatus.OK.status
        )
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(100)
        return configId
    }

    fun `test Get single notification config as part of path`() {
        val configId = createConfig()
        Thread.sleep(1000)
        // Get notification config
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)
    }

    fun `test Get single absent notification config should fail as part of path`() {
        val configId = createConfig()
        Thread.sleep(1000)
        // Get notification config with absent id
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/${configId}extra",
            "",
            RestStatus.NOT_FOUND.status
        )
    }

    fun `test Get single notification config as part of query`() {
        val configId = createConfig()
        Thread.sleep(1000)
        // Get notification config with query parameter
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_id=$configId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(configId, getConfigResponse)
        Thread.sleep(100)
    }

    fun `test Get single absent notification config should fail as part of query`() {
        val configId = createConfig()
        Thread.sleep(1000)
        // Get notification config with query parameter with absent id
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_id=${configId}extra",
            "",
            RestStatus.NOT_FOUND.status
        )
    }

    fun `test Get all notification config`() {
        val configIds: Set<String> = (1..20).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getAllConfigResponse, configIds.size)
    }

    fun `test Get paginated notification config using from_index and max_items`() {
        val firstConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val secondConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val thirdConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val allConfigIds = firstConfigIds.union(secondConfigIds).union(thirdConfigIds)
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(allConfigIds, getAllConfigResponse, allConfigIds.size)
        Thread.sleep(100)

        // Get first 10 notification configs
        val getFirstConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=0&max_items=10",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(firstConfigIds, getFirstConfigResponse, allConfigIds.size)

        // Get first 10 notification configs without from_index
        val getFirstConfigResponseWithoutFromIndex = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?max_items=10",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(firstConfigIds, getFirstConfigResponseWithoutFromIndex, allConfigIds.size)

        // Get second 10 notification configs
        val getSecondConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=10&max_items=10",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(secondConfigIds, getSecondConfigResponse, allConfigIds.size)

        // Get third 10 notification configs
        val getThirdConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=20&max_items=10",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(thirdConfigIds, getThirdConfigResponse, allConfigIds.size)

        // Get all items after 10 notification configs
        val getAfter10ConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=10",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(secondConfigIds.union(thirdConfigIds), getAfter10ConfigResponse, allConfigIds.size)
    }

    fun `test Get descending paginated notification config using from_index and max_items`() {
        val firstConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val secondConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val thirdConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        val allConfigIds = firstConfigIds.union(secondConfigIds).union(thirdConfigIds)
        Thread.sleep(1000)

        // Get all notification configs
        val getAllConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(allConfigIds, getAllConfigResponse, allConfigIds.size)
        Thread.sleep(100)

        // Get first 10 notification configs
        val getFirstConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=0&max_items=10&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(thirdConfigIds, getFirstConfigResponse, allConfigIds.size)

        // Get last 10 notification configs
        val getThirdConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=20&max_items=10&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(firstConfigIds, getThirdConfigResponse, allConfigIds.size)

        // Get all items after 10 notification configs
        val getAfter10ConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?from_index=10&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(secondConfigIds.union(firstConfigIds), getAfter10ConfigResponse, allConfigIds.size)
    }

    fun `test Get sorted notification config using metadata keyword sort_field(created_time_ms)`() {
        val id1 = createConfig()
        Thread.sleep(1000)
        val id2 = createConfig()
        Thread.sleep(1000)
        val id3 = createConfig()
        Thread.sleep(1000)
        val id4 = createConfig()
        Thread.sleep(1000)
        val id5 = createConfig()
        Thread.sleep(1000)
        val sortedConfigIds = listOf(id1, id2, id3, id4, id5)

        // Get all notification configs with default sort_order(asc)
        val getDefaultOrderConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=created_time_ms",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getDefaultOrderConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=asc
        val getAscConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=created_time_ms&sort_order=asc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getAscConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=desc
        val getDescConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=created_time_ms&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds.asReversed(), getDescConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get sorted notification config using single keyword sort_field(config_type)`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        Thread.sleep(1000)

        val sortedConfigIds = listOf(chimeId, emailGroupId, slackId, smtpAccountId, webhookId)

        // Get all notification configs with default sort_order(asc)
        val getDefaultOrderConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=config_type",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getDefaultOrderConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=asc
        val getAscConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=config_type&sort_order=asc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getAscConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=desc
        val getDescConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=config_type&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds.asReversed(), getDescConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get sorted notification config using multi keyword sort_field(features)`() {
        val iId = createConfig(features = setOf(INDEX_MANAGEMENT))
        val aId = createConfig(features = setOf(ALERTING))
        val rId = createConfig(features = setOf(REPORTS))
        val iaId = createConfig(features = setOf(INDEX_MANAGEMENT, ALERTING))
        val raId = createConfig(features = setOf(REPORTS, ALERTING))
        val riId = createConfig(features = setOf(REPORTS, INDEX_MANAGEMENT))
        val iarId = createConfig(features = setOf(INDEX_MANAGEMENT, ALERTING, REPORTS))
        Thread.sleep(1000)

        val sortedConfigIds = listOf(aId, iaId, raId, iarId, iId, riId, rId)
        val reverseOrderIds = listOf(rId, raId, riId, iarId, iId, iaId, aId)
        // Get all notification configs with default sort_order(asc)
        val getDefaultOrderConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=feature_list",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getDefaultOrderConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=asc
        val getAscConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=feature_list&sort_order=asc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getAscConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=desc
        val getDescConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=feature_list&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(reverseOrderIds, getDescConfigResponse, sortedConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get sorted notification config using text sort_field(slack_url)`() {
        val configIds: Set<String> = (1..10).map { createConfig(configType = ConfigType.SLACK) }.toSet()
        Thread.sleep(1000)
        val sortedConfigIds = configIds.sorted()

        // Get all notification configs with default sort_order(asc)
        val getDefaultOrderConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=slack.url",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getDefaultOrderConfigResponse, configIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=asc
        val getAscConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=slack.url&sort_order=asc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds, getAscConfigResponse, configIds.size)
        Thread.sleep(100)

        // Get all notification configs with sort_order=desc
        val getDescConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?sort_field=slack.url&sort_order=desc",
            "",
            RestStatus.OK.status
        )
        verifyOrderedConfigList(sortedConfigIds.asReversed(), getDescConfigResponse, configIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using keyword filter_param_list(config_type)`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        Thread.sleep(1000)

        // Get notification configs with one item type
        val getSlackResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_type=slack",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(slackId, getSlackResponse, 1)
        Thread.sleep(100)

        // Get notification configs with 2 item type
        val getSlackOrChimeResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_type=slack,chime",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(setOf(slackId, chimeId), getSlackOrChimeResponse, 2)
        Thread.sleep(100)

        // Get notification configs with 3 item type
        val getWebhookEmailGroupOrSmtpAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_type=webhook,email_group,smtp_account",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(
            setOf(webhookId, emailGroupId, smtpAccountId),
            getWebhookEmailGroupOrSmtpAccountResponse,
            3
        )
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using text filter_param_list(name)`() {
        val configIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get notification configs with common text "sample config name"
        val getCommonResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?name=sample+config+name",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getCommonResponse, configIds.size)
        Thread.sleep(100)

        // Get notification configs with random generated text (id)
        val partialString = configIds.first()
        val partialConfigIds = configIds.filter { it.contains(partialString) }.toSet()
        val getPartialResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?name=$partialString",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(partialConfigIds, getPartialResponse, partialConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using keyword filter_param_list(is_enabled)`() {
        val enabledConfigIds: Set<String> = (1..10).map { createConfig(isEnabled = true) }.toSet()
        val disabledConfigIds: Set<String> = (1..10).map { createConfig(isEnabled = false) }.toSet()
        Thread.sleep(1000)

        // Get notification configs with is_enabled=true
        val getEnabledResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?is_enabled=true",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(enabledConfigIds, getEnabledResponse, enabledConfigIds.size)
        Thread.sleep(100)

        // Get notification configs with is_enabled=false
        val getDisabledResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?is_enabled=false",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(disabledConfigIds, getDisabledResponse, disabledConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using keyword filter_param_list(features)`() {
        val iId = createConfig(features = setOf(INDEX_MANAGEMENT))
        val aId = createConfig(features = setOf(ALERTING))
        val rId = createConfig(features = setOf(REPORTS))
        val iaId = createConfig(features = setOf(INDEX_MANAGEMENT, ALERTING))
        val raId = createConfig(features = setOf(REPORTS, ALERTING))
        val riId = createConfig(features = setOf(REPORTS, INDEX_MANAGEMENT))
        val iarId = createConfig(features = setOf(INDEX_MANAGEMENT, ALERTING, REPORTS))
        Thread.sleep(1000)

        val reportIds = setOf(rId, raId, riId, iarId)
        // Get notification configs with features=Reports
        val getEnabledResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?feature_list=reports",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(reportIds, getEnabledResponse, reportIds.size)
        Thread.sleep(100)

        val imAndAlertsIds = setOf(iId, aId, iaId, raId, riId, iarId)
        // Get notification configs with features=IndexManagement,Alerting
        val getDisabledResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?feature_list=index_management,alerting",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(imAndAlertsIds, getDisabledResponse, imAndAlertsIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using text filter_param_list(description)`() {
        val configIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)

        // Get notification configs with common text "sample description"
        val getCommonResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?description=sample+description",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getCommonResponse, configIds.size)
        Thread.sleep(100)

        // Get notification configs with random generated text (id)
        val partialString = configIds.first()
        val partialConfigIds = configIds.filter { it.contains(partialString) }.toSet()
        val getPartialResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?description=$partialString",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(partialConfigIds, getPartialResponse, partialConfigIds.size)
        Thread.sleep(100)
    }
}
