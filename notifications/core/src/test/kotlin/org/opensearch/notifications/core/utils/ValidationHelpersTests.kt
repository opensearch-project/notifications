/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.utils

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
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
        "9.9.9.9"
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test hosts in denylist`() {
        val ips = listOf(
            "www.amazon.com",
            "127.0.0.1",
            "10.0.0.1",
            "10.11.12.13",
            "172.16.0.1",
            "192.168.0.1",
            "0.0.0.1",
            "9.9.9.9"
        )
        for (ip in ips) {
            assertEquals(
                true,
                org.opensearch.notifications.spi.utils.isHostInDenylist("https://$ip", hostDenyList),
                "address $ip was supposed to be identified as in the deny list, but was not"
            )
        }
    }

    @Test
    fun `test hosts not in denylist`() {
        val urls = listOf("156.4.77.1", "www.something.com")
        for (url in urls) {
            assertEquals(
                false,
                org.opensearch.notifications.spi.utils.isHostInDenylist("https://$url", hostDenyList),
                "address $url was not supposed to be identified as in the deny list, but was"
            )
        }
    }

    @Test
    fun `test hostname gets resolved to ip for denylist`() {
        mockkStatic(InetAddress::class)

        val invalidHost = "invalid.com"
        val expectedAddressesForInvalidHost = arrayOf(
            InetAddress.getByAddress(byteArrayOf(174.toByte(), 120, 0, 0)),
            InetAddress.getByAddress(byteArrayOf(10, 0, 0, 1))
        )
        every { InetAddress.getAllByName(invalidHost) } returns expectedAddressesForInvalidHost
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$invalidHost", hostDenyList))

        val validHost = "valid.com"
        val expectedAddressesForValidHost = arrayOf(
            InetAddress.getByAddress(byteArrayOf(174.toByte(), 12, 0, 0))
        )
        every { InetAddress.getAllByName(validHost) } returns expectedAddressesForValidHost
        assertEquals(false, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$validHost", hostDenyList))
    }

    @Test
    fun `test direct IP in denylist blocked immediately without DNS`() {
        val blockedIp = "127.0.0.1"
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$blockedIp", hostDenyList))
    }

    @Test
    fun `test direct IP in CIDR range blocked immediately`() {
        val ipInRange = "10.5.5.5"
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$ipInRange", hostDenyList))
    }

    @Test
    fun `test hostname NOT in denylist but resolves to blocked IP`() {
        val hostname = "evil-domain.com"

        mockkStatic(InetAddress::class)
        val blockedIp = arrayOf(InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1)))
        every { InetAddress.getAllByName(hostname) } returns blockedIp

        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList))
    }

    @Test
    fun `test direct safe IP allowed without DNS`() {
        val safeIp = "8.8.8.8"
        assertEquals(false, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$safeIp", hostDenyList))
    }

    @Test
    fun `test hostname resolves to safe IP allowed`() {
        val safeHostname = "google.com"

        mockkStatic(InetAddress::class)
        val safeIp = arrayOf(InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8)))
        every { InetAddress.getAllByName(safeHostname) } returns safeIp

        assertEquals(false, org.opensearch.notifications.spi.utils.isHostInDenylist("https://$safeHostname", hostDenyList))
    }

    @Test
    fun `test exact IP match in denylist`() {
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://9.9.9.9", hostDenyList))
    }

    @Test
    fun `test CIDR range boundaries`() {
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://10.0.0.1", hostDenyList))
        assertEquals(true, org.opensearch.notifications.spi.utils.isHostInDenylist("https://10.255.255.255", hostDenyList))
        assertEquals(false, org.opensearch.notifications.spi.utils.isHostInDenylist("https://11.0.0.1", hostDenyList))
    }

    @Test
    fun `test hostname blocked BEFORE DNS resolution when hostname is in denylist`() {
        val blockedHostname = "www.amazon.com"

        mockkStatic(InetAddress::class)
        val safeIpAddress = arrayOf(InetAddress.getByAddress(byteArrayOf(174.toByte(), 12, 0, 0)))
        every { InetAddress.getAllByName(blockedHostname) } returns safeIpAddress

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$blockedHostname", hostDenyList),
            "Hostname $blockedHostname should be blocked by pre-DNS hostname check"
        )
    }

    @Test
    fun `test hostname with multiple resolved IPs where one is blocked`() {
        val hostname = "multi-ip-domain.com"

        mockkStatic(InetAddress::class)
        val mixedIpAddresses = arrayOf(
            InetAddress.getByAddress(byteArrayOf(174.toByte(), 12, 0, 0)),
            InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8)),
            InetAddress.getByAddress(byteArrayOf(10, 0, 0, 50))
        )
        every { InetAddress.getAllByName(hostname) } returns mixedIpAddresses

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Should be blocked when ANY resolved IP is in denylist"
        )
    }

    @Test
    fun `test DNS rebinding attack prevention - hostname resolves to private IP`() {
        val attackerDomain = "attacker-controlled.com"

        mockkStatic(InetAddress::class)
        val privateIpAddresses = arrayOf(
            InetAddress.getByAddress(byteArrayOf(192.toByte(), 168.toByte(), 1, 100))
        )
        every { InetAddress.getAllByName(attackerDomain) } returns privateIpAddresses

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$attackerDomain", hostDenyList),
            "DNS rebinding attack should be blocked by post-DNS check"
        )
    }

    @Test
    fun `test localhost variations are blocked`() {
        val localhostVariations = listOf(
            "127.0.0.1",
            "127.0.0.2",
            "127.1.1.1",
            "127.255.255.254"
        )

        localhostVariations.forEach { localhost ->
            assertEquals(
                true,
                org.opensearch.notifications.spi.utils.isHostInDenylist("https://$localhost", hostDenyList),
                "Localhost variation $localhost should be blocked"
            )
        }
    }

    @Test
    fun `test DNS resolution failure handling`() {
        val unresolveableHostname = "definitely-does-not-exist-12345.com"

        mockkStatic(InetAddress::class)
        every { InetAddress.getAllByName(unresolveableHostname) } throws java.net.UnknownHostException()

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$unresolveableHostname", hostDenyList),
            "Unresolveable hostname not in denylist should not be blocked"
        )
    }

    @Test
    fun `test hostname in denylist is blocked even if DNS resolution fails`() {
        val blockedHostname = "www.amazon.com"

        mockkStatic(InetAddress::class)
        every { InetAddress.getAllByName(blockedHostname) } throws java.net.UnknownHostException()

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$blockedHostname", hostDenyList),
            "Hostname in denylist should be blocked even if DNS fails"
        )
    }

    @Test
    fun `test all IPs in CIDR range are blocked`() {
        val testCases = mapOf(
            "10.0.0.1" to true,
            "10.128.0.1" to true,
            "10.255.255.255" to true,
            "11.0.0.1" to false,
            "172.16.0.1" to true,
            "172.31.255.255" to true,
            "172.15.0.1" to false,
            "172.32.0.1" to false,
            "192.168.0.1" to true,
            "192.168.255.255" to true,
            "192.169.0.1" to false
        )

        testCases.forEach { (ip, shouldBeBlocked) ->
            assertEquals(
                shouldBeBlocked,
                org.opensearch.notifications.spi.utils.isHostInDenylist("https://$ip", hostDenyList),
                "IP $ip should ${if (shouldBeBlocked) "be blocked" else "not be blocked"}"
            )
        }
    }

    @Test
    fun `test safe hostname with multiple safe IPs is allowed`() {
        val safeHostname = "safe-multi-ip.com"

        mockkStatic(InetAddress::class)
        val safeIpAddresses = arrayOf(
            InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8)),
            InetAddress.getByAddress(byteArrayOf(8, 8, 4, 4)),
            InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1))
        )
        every { InetAddress.getAllByName(safeHostname) } returns safeIpAddresses

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$safeHostname", hostDenyList),
            "Safe hostname with all safe IPs should be allowed"
        )
    }

    @Test
    fun `test hostname NOT in denylist with first IP safe but second IP blocked`() {
        val hostname = "tricky-domain.com"

        mockkStatic(InetAddress::class)
        val ipAddresses = arrayOf(
            InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8)),
            InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
        )
        every { InetAddress.getAllByName(hostname) } returns ipAddresses

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Should check all resolved IPs, not just the first one"
        )
    }

    @Test
    fun `test URL with port number is handled correctly`() {
        val testCases = mapOf(
            "https://127.0.0.1:8080" to true,
            "https://10.0.0.1:9200" to true,
            "https://8.8.8.8:443" to false,
            "https://www.amazon.com:443" to true
        )

        testCases.forEach { (url, shouldBeBlocked) ->
            assertEquals(
                shouldBeBlocked,
                org.opensearch.notifications.spi.utils.isHostInDenylist(url, hostDenyList),
                "URL $url should ${if (shouldBeBlocked) "be blocked" else "not be blocked"}"
            )
        }
    }

    @Test
    fun `test exact IP 9-9-9-9 in denylist is blocked`() {
        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://9.9.9.9", hostDenyList),
            "Exact IP 9.9.9.9 should be blocked"
        )

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://9.9.9.8", hostDenyList),
            "IP 9.9.9.8 should not be blocked"
        )

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://9.9.9.10", hostDenyList),
            "IP 9.9.9.10 should not be blocked"
        )
    }

    // IPv6 Tests
    @Test
    fun `test IPv6 mapped IPv4 localhost is blocked`() {
        val hostname = "ipv6-localhost-mapped.test"

        mockkStatic(InetAddress::class)
        val ipv6Bytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0xFF.toByte(), 0xFF.toByte(),
            127, 0, 0, 1
        )
        val ipv6MappedLocalhost = InetAddress.getByAddress(ipv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6MappedLocalhost)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to IPv6-mapped localhost should be blocked"
        )
    }

    @Test
    fun `test IPv6 mapped IPv4 private network 10-0-0-1 is blocked`() {
        val hostname = "ipv6-private-10.test"

        mockkStatic(InetAddress::class)
        val ipv6Bytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0xFF.toByte(), 0xFF.toByte(),
            10, 0, 0, 1
        )
        val ipv6MappedPrivate = InetAddress.getByAddress(ipv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6MappedPrivate)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to IPv6-mapped 10.0.0.1 should be blocked"
        )
    }

    @Test
    fun `test IPv6 mapped IPv4 private network 192-168-1-1 is blocked`() {
        val hostname = "ipv6-private-192.test"

        mockkStatic(InetAddress::class)
        val ipv6Bytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0xFF.toByte(), 0xFF.toByte(),
            192.toByte(), 168.toByte(), 1, 1
        )
        val ipv6MappedPrivate = InetAddress.getByAddress(ipv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6MappedPrivate)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to IPv6-mapped 192.168.1.1 should be blocked"
        )
    }

    @Test
    fun `test IPv6 mapped IPv4 safe IP 8-8-8-8 is allowed`() {
        val hostname = "ipv6-safe.test"

        mockkStatic(InetAddress::class)
        val ipv6Bytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0xFF.toByte(), 0xFF.toByte(),
            8, 8, 8, 8
        )
        val ipv6MappedSafe = InetAddress.getByAddress(ipv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6MappedSafe)

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to IPv6-mapped safe IP should be allowed"
        )
    }

    @Test
    fun `test hostname resolves to mix of IPv4 and IPv6 mapped with one blocked`() {
        val hostname = "dual-stack-mixed.test"

        mockkStatic(InetAddress::class)
        val safeIpv4 = InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8))
        val ipv6MappedBlocked = InetAddress.getByAddress(
            byteArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0xFF.toByte(), 0xFF.toByte(),
                10, 0, 0, 1
            )
        )
        every { InetAddress.getAllByName(hostname) } returns arrayOf(safeIpv4, ipv6MappedBlocked)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname with mix of safe and IPv6-mapped blocked IPs should be blocked"
        )
    }

    @Test
    fun `test pure IPv6 address without IPv4 mapping is handled`() {
        val hostname = "pure-ipv6.test"

        mockkStatic(InetAddress::class)
        val pureIpv6Bytes = byteArrayOf(
            0x20, 0x01,
            0x0d.toByte(), 0xb8.toByte(),
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 1
        )
        val pureIpv6 = InetAddress.getByAddress(pureIpv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(pureIpv6)

        assertEquals(
            false,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to pure IPv6 (not in denylist) should be allowed"
        )
    }

    @Test
    fun `test IPv6 compatible IPv4 localhost is blocked`() {
        val hostname = "ipv6-compatible-localhost.test"

        mockkStatic(InetAddress::class)
        val ipv6Bytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            127, 0, 0, 1
        )
        val ipv6CompatibleLocalhost = InetAddress.getByAddress(ipv6Bytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6CompatibleLocalhost)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Hostname resolving to IPv6-compatible localhost should be blocked"
        )
    }

    @Test
    fun `test IPv6 loopback is blocked when in denylist`() {
        val hostname = "ipv6-loopback.test"
        val denyListWithIpv6Loopback = hostDenyList + listOf("::1")

        mockkStatic(InetAddress::class)
        val ipv6LoopbackBytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1
        )
        val ipv6Loopback = InetAddress.getByAddress(ipv6LoopbackBytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6Loopback)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", denyListWithIpv6Loopback),
            "Hostname resolving to IPv6 loopback should be blocked if in denylist"
        )
    }

    @Test
    fun `test hostname resolves to multiple IPv6 mapped IPs with one blocked`() {
        val hostname = "multi-ipv6.test"

        mockkStatic(InetAddress::class)
        val safeIpv6 = InetAddress.getByAddress(
            byteArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0xFF.toByte(), 0xFF.toByte(),
                8, 8, 8, 8
            )
        )
        val blockedIpv6 = InetAddress.getByAddress(
            byteArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0xFF.toByte(), 0xFF.toByte(),
                127, 0, 0, 1
            )
        )
        every { InetAddress.getAllByName(hostname) } returns arrayOf(safeIpv6, blockedIpv6)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "Should be blocked when any IPv6-mapped IP is in denylist"
        )
    }

    @Test
    fun `test IPv6 with embedded zeros is handled correctly`() {
        val hostname = "ipv6-zeros.test"

        mockkStatic(InetAddress::class)
        val ipv6ZerosBytes = byteArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0,
            0xFF.toByte(), 0xFF.toByte(),
            0, 0, 0, 0
        )
        val ipv6Zeros = InetAddress.getByAddress(ipv6ZerosBytes)
        every { InetAddress.getAllByName(hostname) } returns arrayOf(ipv6Zeros)

        assertEquals(
            true,
            org.opensearch.notifications.spi.utils.isHostInDenylist("https://$hostname", hostDenyList),
            "IPv6-mapped 0.0.0.0 should be blocked"
        )
    }
}
