/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.util
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Webhook

object ConfigEncryptionTransformer {
    private lateinit var fieldEncryptionService: FieldEncryptionService

    fun initialize(fieldEncryptionService: FieldEncryptionService) {
        this.fieldEncryptionService = fieldEncryptionService
    }

    fun encryptConfig(config: NotificationConfig): NotificationConfig {
        return transform(config, fieldEncryptionService::encrypt)
    }

    fun decryptConfig(config: NotificationConfig): NotificationConfig {
        return transform(config, fieldEncryptionService::decrypt)
    }

    private fun transform(config: NotificationConfig, transformString: (String) -> String): NotificationConfig {
        val transformedData = when (config.configType) {
            ConfigType.SLACK,
            ConfigType.MATTERMOST -> {
                val slack = config.configData as Slack
                slack.copy(url = transformString(slack.url))
            }
            ConfigType.CHIME -> {
                val chime = config.configData as Chime
                chime.copy(url = transformString(chime.url))
            }
            ConfigType.MICROSOFT_TEAMS -> {
                val teams = config.configData as MicrosoftTeams
                teams.copy(url = transformString(teams.url))
            }
            ConfigType.WEBHOOK -> {
                val webhook = config.configData as Webhook
                webhook.copy(
                    url = transformString(webhook.url),
                    headerParams = webhook.headerParams.mapValues { (_, value) -> transformString(value) }
                )
            }
            else -> return config
        }
        return config.copy(configData = transformedData)
    }
}
