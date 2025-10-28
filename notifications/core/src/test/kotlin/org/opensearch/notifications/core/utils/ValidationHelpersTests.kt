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

    @Test
    fun `test direct IP in denylist blocked immediately without DNS`() {
        // URL uses direct IP that's in denylist
        val blockedIp = "127.0.0.1"

        // Should be blocked by direct IP check (no DNS resolution needed)
        assertEquals(true, isHostInDenylist("https://$blockedIp", hostDenyList))
    }

    @Test
    fun `test direct IP in CIDR range blocked immediately`() {
        // URL uses direct IP within a CIDR range in denylist
        val ipInRange = "10.5.5.5" // Within 10.0.0.0/8

        // Should be blocked by direct IP check matching CIDR range
        assertEquals(true, isHostInDenylist("https://$ipInRange", hostDenyList))
    }

    @Test
    fun `test hostname NOT in denylist but resolves to blocked IP`() {
        // Hostname is not in denylist (denylist has no hostnames, only IPs)
        val hostname = "evil-domain.com"

        mockkStatic(InetAddress::class)
        // Resolves to blocked IP
        val blockedIp = arrayOf(InetAddress.getByName("127.0.0.1"))
        every { InetAddress.getAllByName(hostname) } returns blockedIp

        // Should be blocked by post-DNS check
        assertEquals(true, isHostInDenylist("https://$hostname", hostDenyList))
    }

    @Test
    fun `test direct safe IP allowed without DNS`() {
        // URL uses direct IP that's NOT in denylist
        val safeIp = "8.8.8.8"

        // Should NOT be blocked
        assertEquals(false, isHostInDenylist("https://$safeIp", hostDenyList))
    }

    @Test
    fun `test hostname resolves to safe IP allowed`() {
        val safeHostname = "google.com"

        mockkStatic(InetAddress::class)
        val safeIp = arrayOf(InetAddress.getByName("8.8.8.8"))
        every { InetAddress.getAllByName(safeHostname) } returns safeIp

        // Should NOT be blocked
        assertEquals(false, isHostInDenylist("https://$safeHostname", hostDenyList))
    }

    @Test
    fun `test exact IP match in denylist`() {
        // Test exact IP that's in denylist (9.9.9.9)
        assertEquals(true, isHostInDenylist("https://9.9.9.9", hostDenyList))
    }

    @Test
    fun `test CIDR range boundaries`() {
        // Test IPs at the boundaries of CIDR ranges
        assertEquals(true, isHostInDenylist("https://10.0.0.1", hostDenyList))
        assertEquals(true, isHostInDenylist("https://10.255.255.255", hostDenyList))
        assertEquals(false, isHostInDenylist("https://11.0.0.1", hostDenyList))
    }
}
