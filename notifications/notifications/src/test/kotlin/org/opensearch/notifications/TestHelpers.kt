/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.core.xcontent.DeprecationHandler
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentParser
import java.io.ByteArrayOutputStream

const val SLACK_URL = "https://hooks.slack.com/services/T000/B000/plain-token"
const val CHIME_URL = "https://hooks.chime.aws/incomingwebhooks/abc?token=xyz"
const val TEAMS_URL = "https://outlook.office.com/webhook/xxx@yyy/IncomingWebhook/zzz/secret"
const val WEBHOOK_URL = "https://api.example.com/notify"

fun getJsonString(xContent: ToXContent): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
        xContent.toXContent(builder, ToXContent.EMPTY_PARAMS)
        builder.close()
        return byteArrayOutputStream.toString("UTF8")
    }
}

inline fun <reified CreateType> createObjectFromJsonString(
    jsonString: String,
    block: (XContentParser) -> CreateType
): CreateType {
    val parser = XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, jsonString)
    parser.nextToken()
    return block(parser)
}

fun createSlackNotificationConfig(url: String): NotificationConfig {
    return NotificationConfig(
        "slack-config",
        "description",
        ConfigType.SLACK,
        configData = Slack(url)
    )
}

fun createWebhookNotificationConfig(url: String, headers: Map<String, String> = emptyMap()): NotificationConfig {
    return NotificationConfig(
        "webhook-config",
        "description",
        ConfigType.WEBHOOK,
        configData = Webhook(url, headers)
    )
}
