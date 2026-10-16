/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.opensearch.common.settings.SecureSetting
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings
import org.opensearch.common.settings.SettingsException
import org.opensearch.core.common.settings.SecureString
import java.util.Collections

internal class NotificationConfigSecrets(settings: Settings) : AutoCloseable {
    companion object {
        const val SETTING_PREFIX = "opensearch.notifications.keystore."

        val SETTING: Setting.AffixSetting<SecureString> = Setting.prefixKeySetting(SETTING_PREFIX) { key ->
            SecureSetting.secureString(key, null)
        }

        private val REFERENCE_PATTERN = Regex("\\$\\{keystore:([A-Za-z0-9_.-]+)}")
    }

    private var secrets: Map<String, CharArray> = load(settings)

    @Synchronized
    fun resolve(configId: String, value: String): String {
        return REFERENCE_PATTERN.replace(value) { matchResult ->
            val alias = matchResult.groupValues[1]
            val settingName = "$configId.$alias"
            val secret = secrets[settingName]
                ?: throw SettingsException("Keystore setting [$SETTING_PREFIX$settingName] referenced by notification configuration is missing")
            String(secret)
        }
    }

    @Synchronized
    fun reload(settings: Settings) {
        val replacement = load(settings)
        val previous = secrets
        secrets = replacement
        clear(previous)
    }

    @Synchronized
    override fun close() {
        clear(secrets)
        secrets = emptyMap()
    }

    private fun load(settings: Settings): Map<String, CharArray> {
        val loaded = mutableMapOf<String, CharArray>()
        try {
            SETTING.getAsMap(settings).forEach { (alias, secureString) ->
                secureString.use {
                    loaded[alias] = it.chars.clone()
                }
            }
            return Collections.unmodifiableMap(loaded)
        } catch (exception: RuntimeException) {
            clear(loaded)
            throw exception
        }
    }

    private fun clear(values: Map<String, CharArray>) {
        values.values.forEach { it.fill('\u0000') }
    }
}
