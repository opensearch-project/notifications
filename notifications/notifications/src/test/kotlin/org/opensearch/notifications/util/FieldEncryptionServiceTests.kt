/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal class FieldEncryptionServiceTests {

    private fun generateKey(): SecretKey {
        val kg = KeyGenerator.getInstance("AES")
        kg.init(256)
        return kg.generateKey()
    }

    private fun serviceWith(key: SecretKey? = generateKey()) = FieldEncryptionService(key)

    @Nested
    inner class IsEncrypted {

        @Test
        fun `returns true for enc-v1 prefix`() {
            val svc = serviceWith()
            assertTrue(svc.isEncrypted("enc:v1:somepayload"))
        }

        @Test
        fun `returns false for plain text`() {
            val svc = serviceWith()
            assertFalse(svc.isEncrypted("https://hooks.slack.com/services/TOKEN"))
        }

        @Test
        fun `returns false for empty string`() {
            val svc = serviceWith()
            assertFalse(svc.isEncrypted(""))
        }

        @Test
        fun `returns false for partial prefix`() {
            val svc = serviceWith()
            assertFalse(svc.isEncrypted("enc:v"))
        }
    }

    @Nested
    inner class Encrypt {

        @Test
        fun `returns string prefixed with enc-v1`() {
            val svc = serviceWith()
            val result = svc.encrypt("super-secret")
            assertTrue(result.startsWith(FieldEncryptionService.ENCRYPTED_PREFIX))
        }

        @Test
        fun `produces unique ciphertext on each call (random IV)`() {
            val svc = serviceWith()
            val plaintext = "https://hooks.slack.com/services/TOKEN"
            val first = svc.encrypt(plaintext)
            val second = svc.encrypt(plaintext)
            assertNotEquals(first, second, "Two encryptions of the same plaintext must differ due to random IV")
        }

        @Test
        fun `in passthrough mode returns plaintext unchanged`() {
            val svc = serviceWith(key = null)
            val plaintext = "my-api-token"
            assertEquals(plaintext, svc.encrypt(plaintext))
        }

        @Test
        fun `in passthrough mode does not add enc-v1 prefix`() {
            val svc = serviceWith(key = null)
            assertFalse(svc.encrypt("value").startsWith(FieldEncryptionService.ENCRYPTED_PREFIX))
        }
    }

    @Nested
    inner class Decrypt {

        @Test
        fun `after encrypt returns original plaintext`() {
            val svc = serviceWith()
            val original = "https://hooks.slack.com/services/MY-SECRET-TOKEN"
            assertEquals(original, svc.decrypt(svc.encrypt(original)))
        }

        @Test
        fun `round-trips unicode and special characters`() {
            val svc = serviceWith()
            val original = "pässwörd!@#\$%^&*()_+= 日本語"
            assertEquals(original, svc.decrypt(svc.encrypt(original)))
        }

        @Test
        fun `returns plaintext value unchanged when value has no enc-v1 prefix`() {
            val svc = serviceWith()
            val plaintext = "some-legacy-value"
            assertEquals(plaintext, svc.decrypt(plaintext))
        }

        @Test
        fun `returns empty string unchanged`() {
            val svc = serviceWith()
            assertEquals("", svc.decrypt(""))
        }

        @Test
        fun `in passthrough mode returns enc-v1 value unchanged`() {
            val svc = serviceWith(key = null)
            val value = "enc:v1:somepayload"
            assertEquals(value, svc.decrypt(value))
        }

        @Test
        fun `throws AEADBadTagException on tampered ciphertext`() {
            val svc = serviceWith()
            val encrypted = svc.encrypt("sensitive-value")
            val prefix = FieldEncryptionService.ENCRYPTED_PREFIX
            val tampered = prefix + encrypted.removePrefix(prefix).reversed()
            assertThrows<Exception> { svc.decrypt(tampered) }
        }

        @Test
        fun `throws AEADBadTagException when wrong key is used`() {
            val encryptSvc = serviceWith(generateKey())
            val decryptSvc = serviceWith(generateKey())
            val encrypted = encryptSvc.encrypt("top-secret")
            assertThrows<AEADBadTagException> { decryptSvc.decrypt(encrypted) }
        }
    }

    @Nested
    inner class Close {

        @Test
        fun `does not throw`() {
            assertDoesNotThrow<Unit> { serviceWith().close() }
        }

        @Test
        fun `is idempotent — can be called multiple times`() {
            val svc = serviceWith()
            assertDoesNotThrow<Unit> {
                svc.close()
                svc.close()
            }
        }
    }
}
