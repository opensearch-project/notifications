/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.config

import org.junit.Assert
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifyMultiConfigIdEquals
import org.opensearch.notifications.verifyOrderedConfigList
import org.opensearch.notifications.verifySingleConfigEquals
import org.opensearch.notifications.verifySingleConfigIdEquals
import org.opensearch.rest.RestRequest
import java.time.Instant

class QueryNotificationConfigIT : PluginRestTestCase() {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

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

    fun `test Get multiple notification config as part of query`() {
        (1..5).map { createConfig() }.toSet()
        Thread.sleep(100)
        (1..5).map { createConfig() }.toSet()
        val configIds: Set<String> = (1..5).map { createConfig() }.toSet()
        (1..5).map { createConfig() }.toSet()
        Thread.sleep(1000)
        // Get notification config with query parameter
        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_id_list=${configIds.joinToString(",")}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(configIds, getConfigResponse, configIds.size)
        Thread.sleep(100)
    }

    fun `test Get all notification config`() {
        val configIds: Set<String> = (1..20).map { createConfig() }.toSet()
        refreshAllIndices()

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
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        Thread.sleep(1000)

        val sortedConfigIds = listOf(chimeId, emailGroupId, microsoftTeamsId, slackId, smtpAccountId, webhookId)

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
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
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
        val getMicrosoftTeamsOrChimeResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?config_type=microsoft_teams,chime",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(setOf(microsoftTeamsId, chimeId), getMicrosoftTeamsOrChimeResponse, 2)
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

    fun `test Get filtered notification config using keyword filter_param_list(last_updated_time_ms)`() {
        val initialTime = Instant.now()
        val initialConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val middleTime = Instant.now()
        val middleConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val finalTime = Instant.now()
        val finalConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val endTime = Instant.now()

        // Get notification configs between initialTime..middleTime
        val getFirstResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?last_updated_time_ms=${initialTime.toEpochMilli()}..${middleTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(initialConfigIds, getFirstResponse, initialConfigIds.size)
        Thread.sleep(100)

        // Get notification configs between middleTime..finalTime
        val getSecondResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?last_updated_time_ms=${middleTime.toEpochMilli()}..${finalTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(middleConfigIds, getSecondResponse, middleConfigIds.size)
        Thread.sleep(100)

        // Get notification configs between finalTime..endTime
        val getThirdResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?last_updated_time_ms=${finalTime.toEpochMilli()}..${endTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(finalConfigIds, getThirdResponse, finalConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using keyword filter_param_list(created_time_ms)`() {
        val initialTime = Instant.now()
        val initialConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val middleTime = Instant.now()
        val middleConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val finalTime = Instant.now()
        val finalConfigIds: Set<String> = (1..10).map { createConfig() }.toSet()
        Thread.sleep(1000)
        val endTime = Instant.now()

        // Get notification configs between initialTime..middleTime
        val getFirstResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?created_time_ms=${initialTime.toEpochMilli()}..${middleTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(initialConfigIds, getFirstResponse, initialConfigIds.size)
        Thread.sleep(100)

        // Get notification configs between middleTime..finalTime
        val getSecondResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?created_time_ms=${middleTime.toEpochMilli()}..${finalTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(middleConfigIds, getSecondResponse, middleConfigIds.size)
        Thread.sleep(100)

        // Get notification configs between finalTime..endTime
        val getThirdResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?created_time_ms=${finalTime.toEpochMilli()}..${endTime.toEpochMilli() - 1}",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(finalConfigIds, getThirdResponse, finalConfigIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using keyword filter_param_list(internal config fields)`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        Thread.sleep(1000)

        // Get notification configs using slack.url
        val getSlackResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?slack.url=$slackId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(slackId, getSlackResponse, 1)
        Thread.sleep(100)

        // Get notification configs using chime.url
        val getChimeResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?chime.url=$chimeId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(chimeId, getChimeResponse, 1)
        Thread.sleep(100)

        // Get notification configs using microsoft_teams.url
        val getMicrosoftTeamsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?microsoft_teams.url=$microsoftTeamsId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(microsoftTeamsId, getMicrosoftTeamsResponse, 1)
        Thread.sleep(100)

        // Get notification configs using webhook.url
        val getWebhookResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?webhook.url=$webhookId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(webhookId, getWebhookResponse, 1)
        Thread.sleep(100)

        // Get notification configs using email_group.recipient_list.recipient
        val getEmailGroupResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?email_group.recipient_list.recipient=$emailGroupId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(emailGroupId, getEmailGroupResponse, 1)
        Thread.sleep(100)

        // Get notification configs using smtp_account.from_address
        val getSmtpAccountResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?smtp_account.from_address=$smtpAccountId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(smtpAccountId, getSmtpAccountResponse, 1)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using query`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val allIds = setOf(slackId, chimeId, microsoftTeamsId, webhookId, emailGroupId, smtpAccountId)
        val urlIds = setOf(slackId, chimeId, microsoftTeamsId, webhookId)
        val recipientIds = setOf(emailGroupId)
        val fromIds = setOf(emailGroupId, smtpAccountId)
        val domainIds = setOf(microsoftTeamsId, webhookId, smtpAccountId)
        Thread.sleep(1000)

        // Get notification configs using query=slack
        val getSlackResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=slack",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigIdEquals(slackId, getSlackResponse)
        Thread.sleep(100)

        // Get notification configs using query=sample
        val getAllResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=sample",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(allIds, getAllResponse, allIds.size)
        Thread.sleep(100)

        // Get notification configs using query=sample_*
        val getUrlResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=sample_*",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(urlIds, getUrlResponse, urlIds.size)
        Thread.sleep(100)

        // Get notification configs using query=recipient1
        val getRecipientResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=recipient1",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(recipientIds, getRecipientResponse, recipientIds.size)
        Thread.sleep(100)

        // Get notification configs using query=from.com
        val getFromResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=from.com",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(fromIds, getFromResponse, fromIds.size)
        Thread.sleep(100)

        // Get notification configs using query=*.domain.*
        val getDomainResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?query=*.domain.*",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(domainIds, getDomainResponse, domainIds.size)
        Thread.sleep(100)
    }

    fun `test Get filtered notification config using text_query`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val microsoftTeamsId = createConfig(configType = ConfigType.MICROSOFT_TEAMS)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val allIds = setOf(slackId, chimeId, microsoftTeamsId, webhookId, emailGroupId, smtpAccountId)
        val urlIds = setOf(slackId, chimeId, microsoftTeamsId, webhookId)
        val recipientIds = setOf(emailGroupId)
        val fromIds = setOf(emailGroupId, smtpAccountId)
        val domainIds = setOf(microsoftTeamsId, webhookId, smtpAccountId)
        Thread.sleep(1000)

        // Get notification configs using text_query=slack should not return any item
        val getSlackResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=slack",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(setOf(), getSlackResponse, 0)
        Thread.sleep(100)

        // Get notification configs using text_query=sample
        val getAllResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=sample",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(allIds, getAllResponse, allIds.size)
        Thread.sleep(100)

        // Get notification configs using text_query=sample_*
        val getUrlResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=sample_*",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(urlIds, getUrlResponse, urlIds.size)
        Thread.sleep(100)

        // Get notification configs using text_query=recipient1
        val getRecipientResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=recipient1",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(recipientIds, getRecipientResponse, recipientIds.size)
        Thread.sleep(100)

        // Get notification configs using text_query=from.com
        val getFromResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=from.com",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(fromIds, getFromResponse, fromIds.size)
        Thread.sleep(100)

        // Get notification configs using text_query=*.domain.*
        val getDomainResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs?text_query=*.domain.*",
            "",
            RestStatus.OK.status
        )
        verifyMultiConfigIdEquals(domainIds, getDomainResponse, domainIds.size)
        Thread.sleep(100)
    }

    fun `test Get single absent config should fail and then create a config using absent id should pass`() {
        val absentId = "absent_id"
        Thread.sleep(1000)
        // Get notification config with absent id
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$absentId",
            "",
            RestStatus.NOT_FOUND.status
        )

        Thread.sleep(1000)

        // Create sample config request reference
        val sampleChime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        val referenceObject = NotificationConfig(
            "this is a sample config name",
            "this is a sample config description",
            ConfigType.CHIME,
            isEnabled = true,
            configData = sampleChime
        )

        // Create chime notification config
        val createRequestJsonString = """
        {
            "config_id":"$absentId",
            "config":{
                "name":"${referenceObject.name}",
                "description":"${referenceObject.description}",
                "config_type":"chime",
                "is_enabled":${referenceObject.isEnabled},
                "chime":{"url":"${(referenceObject.configData as Chime).url}"}
            }
        }
        """.trimIndent()
        val createdConfigId = createConfigWithRequestJsonString(createRequestJsonString)
        Assert.assertEquals(absentId, createdConfigId)
        Thread.sleep(1000)

        // Get chime notification config

        val getConfigResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/configs/$absentId",
            "",
            RestStatus.OK.status
        )
        verifySingleConfigEquals(absentId, referenceObject, getConfigResponse)
    }
}
