/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValidationHelpersTests {

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
}
