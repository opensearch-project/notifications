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
internal class ValidationHelpersTests {
    private val GOOGLE_URL = "https://www.google.com"
    private val INVALID_URL = "www.invalid.com"
    private val VALID_NOT_FQDN_URL = "https://odfe-es-client-service:9200/"
    private val SAMPLE_URL = "https://sample.url:1234"
    private val LOCAL_HOST_URL = "https://localhost:6060"
    private val LOCAL_HOST_EXTENDED = "https://localhost:6060/service"
    private val WEBHOOK_URL = "https://test-webhook.com:1234/subdirectory?param1=value1&param2=&param3=value3"
    private val CHIME_URL = "https://domain.com/sample_chime_url#1234567890"
    private val MICROSOFT_TEAMS_INCOMING_WEBHOOK = "https://8m7xqz.webhook.office.com/webhookb2/b0885113-57f8-4b61-8f3a-bdf3f4ae2831@500d1839-8666-4320-9f55-59d8838ad8db/IncomingWebhook/84637be48f4245c09b82e735b2cd9335/b7e1bf56-6634-422c-abe8-402e6e95fc68"
    private val TELEGRAM_TOKEN = "5910159857:AAF9qpqgPp3SZE_gAsKvhjy0uVgJ9zLLe7"
    private val TELEGRAM_URL = "https://t.me/notificationPluginBot"

    private val hostDenyList = listOf(
        "127.0.0.0/8",
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "0.0.0.0/8",
        "9.9.9.9" // ip
    )

    @Test
    fun `test ips in denylist`() {
        val ips = listOf(
            "127.0.0.1", // 127.0.0.0/8
            "10.0.0.1", // 10.0.0.0/8
            "10.11.12.13", // 10.0.0.0/8
            "172.16.0.1", // "172.16.0.0/12"
            "192.168.0.1", // 192.168.0.0/16"
            "0.0.0.1", // 0.0.0.0/8
            "9.9.9.9"
        )
        for (ip in ips) {
            assertEquals(true, isHostInDenylist("https://$ip", hostDenyList))
        }
    }

    @Test
    fun `test hostname gets resolved to ip for denylist`() {
        val invalidHost = "invalid.com"
        mockkStatic(InetAddress::class)
        every { InetAddress.getByName(invalidHost).hostAddress } returns "10.0.0.1" // 10.0.0.0/8
        assertEquals(true, isHostInDenylist("https://$invalidHost", hostDenyList))

        val validHost = "valid.com"
        every { InetAddress.getByName(validHost).hostAddress } returns "174.12.0.0"
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
    fun `validator identifies webhook url as valid`() {
        assert(isValidUrl(MICROSOFT_TEAMS_WEBHOOK))
    }

    @Test
    fun `validator identifies chime url as valid`() {
        assert(isValidUrl(CHIME_URL))
    }
    @Test
    fun `validator identifies telegram token as valid`() {
        assert(isValidTelegramToken(TELEGRAM_TOKEN))
    }
}
