/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.spi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValidationHelpersTests {

    private val hostDenyList = listOf(
        "127.0.0.0/8",
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "0.0.0.0/8",
        "9.9.9.9",
        "localhost",
        "www.amazon.com"
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
    fun `test url with host not in host_deny_list should return false`() {
        val urls = listOf("https://www.example.com", "https://mytest.com")
        for (url in urls) {
            assertEquals(false, isHostInDenylist(url, hostDenyList))
        }
    }

    @Test
    fun `test url with host in host_deny_list should return true`() {
        val urls = listOf("https://www.amazon.com", "https://localhost/sth/xxx")
        for (url in urls) {
            assertEquals(true, isHostInDenylist(url, hostDenyList))
        }
    }
}
