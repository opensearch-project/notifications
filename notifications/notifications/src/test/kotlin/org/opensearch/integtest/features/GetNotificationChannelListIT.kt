/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.features

import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.core.rest.RestStatus
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.verifyChannelIdEquals
import org.opensearch.rest.RestRequest

class GetNotificationChannelListIT : PluginRestTestCase() {

    fun `test POST channel list should result in error`() {
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/channels",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test PUT channel list should result in error`() {
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/channels",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test getChannelList should return only channels`() {
        val slackId = createConfig(configType = ConfigType.SLACK)
        val chimeId = createConfig(configType = ConfigType.CHIME)
        val webhookId = createConfig(configType = ConfigType.WEBHOOK)
        val emailGroupId = createConfig(configType = ConfigType.EMAIL_GROUP)
        val smtpAccountId = createConfig(configType = ConfigType.SMTP_ACCOUNT)
        val emailId = createConfig(
            configType = ConfigType.EMAIL,
            smtpAccountId = smtpAccountId,
            emailGroupId = setOf(emailGroupId)
        )
        Thread.sleep(1000)

        val channelIds = setOf(slackId, chimeId, webhookId, emailId)
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/channels",
            "",
            RestStatus.OK.status
        )
        Thread.sleep(100)
        verifyChannelIdEquals(channelIds, response, channelIds.size)
        Thread.sleep(100)
    }
}
