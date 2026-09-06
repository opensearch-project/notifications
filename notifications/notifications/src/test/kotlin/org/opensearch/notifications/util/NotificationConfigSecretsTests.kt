/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.opensearch.common.settings.SecureSettings
import org.opensearch.common.settings.Settings
import org.opensearch.common.settings.SettingsException
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.core.common.settings.SecureString
import java.io.InputStream

internal class NotificationConfigSecretsTests {
    @Test
    fun `resolves embedded keystore references`() {
        val secrets = NotificationConfigSecrets(settings(mapOf("config-1.slack.path" to "T000/B000/secret")))
        val slack = Slack("https://hooks.slack.com/services/\${keystore:slack.path}")

        val resolved = secrets.resolve("config-1", slack.url)

        assertEquals("https://hooks.slack.com/services/T000/B000/secret", resolved)
    }

    @Test
    fun `resolves complete header value references`() {
        val secrets = NotificationConfigSecrets(settings(mapOf("config-1.webhook.token" to "Bearer secret")))

        assertEquals("Bearer secret", secrets.resolve("config-1", "\${keystore:webhook.token}"))
    }

    @Test
    fun `references are scoped to the notification config`() {
        val secrets = NotificationConfigSecrets(settings(mapOf("config-1.webhook.token" to "Bearer secret")))

        assertThrows(SettingsException::class.java) {
            secrets.resolve("config-2", "\${keystore:webhook.token}")
        }
    }

    @Test
    fun `reload replaces secret values`() {
        val secrets = NotificationConfigSecrets(settings(mapOf("config-1.slack.path" to "old")))

        secrets.reload(settings(mapOf("config-1.slack.path" to "new")))

        assertEquals("https://example.com/new", secrets.resolve("config-1", "https://example.com/\${keystore:slack.path}"))
    }

    @Test
    fun `missing keystore reference is rejected`() {
        val secrets = NotificationConfigSecrets(Settings.EMPTY)

        assertThrows(SettingsException::class.java) {
            secrets.resolve("config-1", "\${keystore:missing}")
        }
    }

    @Test
    fun `plain values pass through unchanged`() {
        val secrets = NotificationConfigSecrets(Settings.EMPTY)

        assertEquals("https://example.com/path", secrets.resolve("config-1", "https://example.com/path"))
    }

    private fun settings(values: Map<String, String>): Settings {
        return Settings.builder().setSecureSettings(TestSecureSettings(values)).build()
    }

    private class TestSecureSettings(private val values: Map<String, String>) : SecureSettings {
        override fun isLoaded() = true

        override fun getSettingNames(): Set<String> = values.keys.map { NotificationConfigSecrets.SETTING_PREFIX + it }.toSet()

        override fun getString(setting: String): SecureString {
            return SecureString(values.getValue(setting.removePrefix(NotificationConfigSecrets.SETTING_PREFIX)).toCharArray())
        }

        override fun getFile(setting: String): InputStream = throw UnsupportedOperationException()

        override fun getSHA256Digest(setting: String): ByteArray = throw UnsupportedOperationException()

        override fun close() = Unit
    }
}
