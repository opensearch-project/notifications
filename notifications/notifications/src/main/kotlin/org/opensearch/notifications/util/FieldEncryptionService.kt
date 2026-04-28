/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.security.auth.Destroyable

/**
 * Encrypts and decrypts sensitive notification-channel configuration fields
 * using AES-256-GCM (an authenticated cipher — integrity is verified on every
 * decryption, so any tampering is detected automatically).
 *
 * Ciphertext wire format:
 *   "enc:v1:<Base64(nonce[12 bytes] || ciphertext || GCM-tag[16 bytes])>"
 *
 * The nonce (Initialization Vector / IV) is a 12-byte random value generated
 * fresh on every [encrypt] call. It guarantees that two encryptions of the same
 * plaintext with the same key always produce different ciphertexts, which is a
 * fundamental requirement for semantic security. The nonce does not need to be
 * secret — it is prepended to the ciphertext in plain form so that [decrypt]
 * can reconstruct the same cipher state.
 *
 * The version tag (`v1`) in the prefix allows future algorithm migrations
 * without breaking existing stored values (see [decrypt] passthrough behaviour).
 *
 * **Passthrough mode** — when constructed with `secretKey = null` the service
 * performs no encryption or decryption. [encrypt] returns the plaintext
 * unchanged; [decrypt] returns the value unchanged (even if it carries the
 * `enc:v1:` prefix). This is intentional: it lets operators roll out the
 * feature flag and provision keystore entries without causing data loss.
 *
 * @param secretKey a 256-bit AES key, or `null` to operate in passthrough mode.
 */
class FieldEncryptionService(private var secretKey: SecretKey?) : AutoCloseable {

    private val log by logger(javaClass)

    companion object {
        /** Sentinel prefix that marks an encrypted field value. */
        const val ENCRYPTED_PREFIX = "enc:v1:"

        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_NONCE_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }

    /**
     * Returns `true` when [value] was produced by [encrypt] and carries the
     * `enc:v1:` sentinel prefix.
     */
    fun isEncrypted(value: String): Boolean = value.startsWith(ENCRYPTED_PREFIX)

    /**
     * Encrypts [plaintext] with AES-256-GCM using a freshly generated random nonce
     * on every call (so identical inputs produce different ciphertexts).
     *
     * Returns the plaintext unchanged when operating in passthrough mode.
     */
    fun encrypt(plaintext: String): String {
        if (secretKey == null) {
            log.warn("$LOG_PREFIX:FieldEncryptionService passthrough mode — field will not be encrypted")
            return plaintext
        }

        val nonce = ByteArray(GCM_NONCE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce))
        // JCE appends the 16-byte GCM auth tag to the end of the returned byte array
        val ciphertextWithTag = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val combined = nonce + ciphertextWithTag
        return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypts a value previously produced by [encrypt].
     *
     * - If [value] does **not** start with `enc:v1:` it is returned as-is.
     *   This makes reads backward-compatible with existing plain-text records.
     * - If no key is configured (passthrough mode) the value is returned as-is,
     *   even when it carries the encrypted prefix.
     * - Throws [javax.crypto.AEADBadTagException] (a subtype of
     *   [javax.crypto.BadPaddingException]) when the auth tag does not verify —
     *   indicating either a wrong key or corrupted/tampered ciphertext.
     */
    fun decrypt(value: String): String {
        if (!isEncrypted(value)) {
            // Plain-text legacy value — return unchanged (backward-compatible)
            return value
        }
        if (secretKey == null) {
            log.warn("$LOG_PREFIX:FieldEncryptionService passthrough mode — cannot decrypt enc:v1: value, returning as-is")
            return value
        }

        val combined = Base64.getDecoder().decode(value.removePrefix(ENCRYPTED_PREFIX))
        val nonce = combined.copyOfRange(0, GCM_NONCE_LENGTH_BYTES)
        val ciphertextWithTag = combined.copyOfRange(GCM_NONCE_LENGTH_BYTES, combined.size)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce))
        return String(cipher.doFinal(ciphertextWithTag), Charsets.UTF_8)
    }

    /**
     * Zeroizes the key material held in memory.
     *
     * If the [SecretKey] implementation also implements [Destroyable] (which
     * [javax.crypto.spec.SecretKeySpec] does since Java 8) its internal byte
     * array is overwritten with zeros so that a heap dump taken after this call
     * will not reveal the key.
     */
    override fun close() {
        val key = secretKey ?: return
        if (key is Destroyable && !key.isDestroyed) {
            try {
                key.destroy()
            } catch (e: Exception) {
                log.warn("$LOG_PREFIX:FieldEncryptionService failed to destroy key material: ${e.message}")
            }
        }
        secretKey = null
    }
}
