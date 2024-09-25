/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.utils

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.InetAddress

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

    @Test
    fun `test hostname gets resolved to ip for denylist`() {
        val expectedAddressesForInvalidHost = arrayOf(
            InetAddress.getByName("174.120.0.0"),
            InetAddress.getByName("10.0.0.1")
        )
        val expectedAddressesForValidHost = arrayOf(
            InetAddress.getByName("174.12.0.0")
        )

        mockkStatic(InetAddress::class)
        val invalidHost = "invalid.com"
        every { InetAddress.getAllByName(invalidHost) } returns expectedAddressesForInvalidHost
        assertEquals(true, isHostInDenylist("https://$invalidHost", hostDenyList))

        val validHost = "valid.com"
        every { InetAddress.getAllByName(validHost) } returns expectedAddressesForValidHost
        assertEquals(false, isHostInDenylist("https://$validHost", hostDenyList))
    }
}
