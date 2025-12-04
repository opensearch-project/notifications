/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.utils

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.net.MalformedURLException

const val GOOGLE_URL = "https://www.google.com"
const val INVALID_URL = "www.invalid.com"
const val VALID_NOT_FQDN_URL = "https://odfe-es-client-service:9200/"
const val SAMPLE_URL = "https://sample.url:1234"
const val LOCAL_HOST_URL = "https://localhost:6060"
const val LOCAL_HOST_EXTENDED = "https://localhost:6060/service"
const val WEBHOOK_URL = "https://test-webhook.com:1234/subdirectory?param1=value1&param2=&param3=value3"
const val CHIME_URL = "https://domain.com/sample_chime_url#1234567890"
const val MICROSOFT_TEAMS_WEBHOOK_URL = "https://test.webhook.office.com/webhookb2/12345678/IncomingWebhook/87654321"

internal class ValidationHelpersTests {
    private val hostDenyList =
        listOf(
            "127.0.0.0/8",
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "0.0.0.0/8",
            "9.9.9.9", // ip
        )

    @Test
    fun `test ips in denylist`() {
        val ips =
            listOf(
                "127.0.0.1", // 127.0.0.0/8
                "10.0.0.1", // 10.0.0.0/8
                "10.11.12.13", // 10.0.0.0/8
                "172.16.0.1", // "172.16.0.0/12"
                "192.168.0.1", // 192.168.0.0/16"
                "0.0.0.1", // 0.0.0.0/8
                "9.9.9.9",
            )
        for (ip in ips) {
            assertEquals(true, isHostInDenylist("https://$ip", hostDenyList))
        }
    }

    @Test
    fun `test hostname gets resolved to ip for denylist`() {
        val expectedAddressesForInvalidHost =
            arrayOf(
                InetAddress.getByName("174.120.0.0"),
                InetAddress.getByName("10.0.0.1"),
            )
        val expectedAddressesForValidHost =
            arrayOf(
                InetAddress.getByName("174.12.0.0"),
            )

        mockkStatic(InetAddress::class)
        val invalidHost = "invalid.com"
        every { InetAddress.getAllByName(invalidHost) } returns expectedAddressesForInvalidHost
        assertEquals(true, isHostInDenylist("https://$invalidHost", hostDenyList))

        val validHost = "valid.com"
        every { InetAddress.getAllByName(validHost) } returns expectedAddressesForValidHost
        assertEquals(false, isHostInDenylist("https://$validHost", hostDenyList))
    }

    @Test
    fun `validator identifies invalid url as invalid`() {
        assertThrows<MalformedURLException> { isValidUrl(INVALID_URL) }
    }

    @Test
    fun `validator identifies valid google url as valid`() {
        assert(isValidUrl(GOOGLE_URL))
    }

    @Test
    fun `validator identifies non FQDN as valid`() {
        assert(isValidUrl(VALID_NOT_FQDN_URL))
    }

    @Test
    fun `validator identifies sample url as valid`() {
        assert(isValidUrl(SAMPLE_URL))
    }

    @Test
    fun `validator identifies localhost url as valid`() {
        assert(isValidUrl(LOCAL_HOST_URL))
    }

    @Test
    fun `validator identifies localhost extended url as valid`() {
        assert(isValidUrl(LOCAL_HOST_EXTENDED))
    }

    @Test
    fun `validator identifies webhook url as valid`() {
        assert(isValidUrl(WEBHOOK_URL))
    }

    @Test
    fun `validator identifies chime url as valid`() {
        assert(isValidUrl(CHIME_URL))
    }

    @Test
    fun `validator identifies microsoft teams url as valid`() {
        assert(isValidUrl(MICROSOFT_TEAMS_WEBHOOK_URL))
    }
}
