/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.settings.MockSecureSettings
import org.opensearch.common.settings.Settings
import org.opensearch.notifications.util.FieldEncryptionService
import java.util.Base64
import javax.crypto.KeyGenerator

internal class FieldEncryptionKeyProvisioningTests {

    private val keySettingName = "opensearch.notifications.field_encryption_key"
    private val previousKeySettingName = "opensearch.notifications.field_encryption_key_previous"

    @Test
    fun `FIELD_ENCRYPTION_KEY is included in getAllSettings`() {
        assertTrue(PluginSettings.getAllSettings().contains(PluginSettings.FIELD_ENCRYPTION_KEY))
    }

    @Test
    fun `FIELD_ENCRYPTION_KEY_PREVIOUS is included in getAllSettings`() {
        assertTrue(PluginSettings.getAllSettings().contains(PluginSettings.FIELD_ENCRYPTION_KEY_PREVIOUS))
    }

    @Test
    fun `FIELD_ENCRYPTION_KEY uses the opensearch-notifications key prefix`() {
        assertTrue(PluginSettings.FIELD_ENCRYPTION_KEY.key.startsWith("opensearch.notifications."))
    }

    @Test
    fun `buildFieldEncryptionService returns passthrough service when key is absent from keystore`() {
        val settings = Settings.builder().build()
        val service = PluginSettings.buildFieldEncryptionService(settings)

        val plaintext = "sensitive-value"
        // passthrough mode: encrypt is a no-op — no enc:v1: prefix added
        assertFalse(service.encrypt(plaintext).startsWith(FieldEncryptionService.ENCRYPTED_PREFIX))
    }

    @Test
    fun `buildFieldEncryptionService returns encrypting service when 256-bit key is configured`() {
        val settings = settingsWithKey(generate256BitKeyBase64())
        val service = PluginSettings.buildFieldEncryptionService(settings)

        assertTrue(service.encrypt("sensitive-value").startsWith(FieldEncryptionService.ENCRYPTED_PREFIX))
    }

    @Test
    fun `buildFieldEncryptionService round-trips plaintext correctly with configured key`() {
        val settings = settingsWithKey(generate256BitKeyBase64())
        val service = PluginSettings.buildFieldEncryptionService(settings)

        val original = "https://hooks.slack.com/services/MY-TOKEN"
        val roundTripped = service.decrypt(service.encrypt(original))
        assertTrue(roundTripped == original)
    }

    @Test
    fun `buildFieldEncryptionService supports active-previous fallback decryption`() {
        val oldKey = generate256BitKeyBase64()
        val newKey = generate256BitKeyBase64()
        val oldService = PluginSettings.buildFieldEncryptionService(settingsWithKey(oldKey))
        val rotatingService = PluginSettings.buildFieldEncryptionService(settingsWithKeys(newKey, oldKey))

        val encryptedWithOldKey = oldService.encrypt("https://hooks.slack.com/services/OLD")
        assertTrue(rotatingService.decrypt(encryptedWithOldKey) == "https://hooks.slack.com/services/OLD")
    }

    private fun generate256BitKeyBase64(): String {
        val kg = KeyGenerator.getInstance("AES")
        kg.init(256)
        return Base64.getEncoder().encodeToString(kg.generateKey().encoded)
    }

    private fun settingsWithKey(base64Key: String): Settings {
        val secureSettings = MockSecureSettings()
        secureSettings.setString(keySettingName, base64Key)
        return Settings.builder().setSecureSettings(secureSettings).build()
    }

    private fun settingsWithKeys(activeBase64Key: String, previousBase64Key: String): Settings {
        val secureSettings = MockSecureSettings()
        secureSettings.setString(keySettingName, activeBase64Key)
        secureSettings.setString(previousKeySettingName, previousBase64Key)
        return Settings.builder().setSecureSettings(secureSettings).build()
    }
}
