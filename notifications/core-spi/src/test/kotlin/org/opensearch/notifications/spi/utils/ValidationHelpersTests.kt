/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    private val hostDentyList = listOf(
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
            assertEquals(true, isHostInDenylist("https://$ip", hostDentyList))
        }
    }

    @Test
    fun `test url in denylist`() {
        val urls = listOf("https://www.amazon.com", "https://mytest.com", "https://mytest.com")
        for (url in urls) {
            assertEquals(false, isHostInDenylist(url, hostDentyList))
        }
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
}
