/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValidationHelpersTests {

    private val hostDenyList = listOf(
        "www.amazon.com",
        "127.0.0.0/8",
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "0.0.0.0/8",
        "9.9.9.9" // ip
    )

    @Test
    fun `test hosts in denylist`() {
        val ips = listOf(
            "www.amazon.com",
            "127.0.0.1", // 127.0.0.0/8
            "10.0.0.1", // 10.0.0.0/8
            "10.11.12.13", // 10.0.0.0/8
            "172.16.0.1", // "172.16.0.0/12"
            "192.168.0.1", // 192.168.0.0/16"
            "0.0.0.1", // 0.0.0.0/8
            "9.9.9.9"
        )
        for (ip in ips) {
            assertEquals(true, isHostInDenylist("https://$ip", hostDenyList), "address $ip was supposed to be identified as in the deny list, but was not")
        }
    }

    @Test
    fun `test hosts not in denylist`() {
        val urls = listOf("156.4.77.1", "www.something.com")
        for (url in urls) {
            assertEquals(false, isHostInDenylist("https://$url", hostDenyList), "address $url was not supposed to be identified as in the deny list, but was")
        }
    }
}
