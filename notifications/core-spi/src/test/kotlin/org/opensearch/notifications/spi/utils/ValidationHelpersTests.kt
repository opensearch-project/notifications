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
    private val MICROSOFT_TEAMS_WEBHOOK_URL = "https://test.webhook.office.com/webhookb2/12345678/IncomingWebhook/87654321"

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

    @Test
    fun `test hostname blocked BEFORE DNS resolution when hostname is in denylist`() {
        // Test that a hostname in the denylist is blocked BEFORE DNS resolution happens
        val blockedHostname = "blocked-domain.com"
        val hostDenyListWithHostname = hostDenyList + listOf(blockedHostname)
        
        // Mock DNS resolution to return a safe IP (should not matter, hostname check should happen first)
        mockkStatic(InetAddress::class)
        val safeIpAddress = arrayOf(InetAddress.getByName("174.12.0.0")) // Not in denylist
        every { InetAddress.getAllByName(blockedHostname) } returns safeIpAddress
        
        // Should be blocked because hostname is in denylist (pre-DNS check)
        assertEquals(true, isHostInDenylist("https://$blockedHostname", hostDenyListWithHostname))
    }

    @Test
    fun `test hostname blocked AFTER DNS resolution when resolved IP is in denylist`() {
        // Test that a hostname NOT in denylist is blocked AFTER DNS resolution 
        // when it resolves to an IP that IS in the denylist
        val legitimateHostname = "legitimate-looking.com"
        
        // Mock DNS resolution to return a blocked IP (e.g., localhost)
        mockkStatic(InetAddress::class)
        val blockedIpAddress = arrayOf(
            InetAddress.getByName("127.0.0.1"), // In denylist: 127.0.0.0/8
            InetAddress.getByName("174.12.0.0")  // Safe IP
        )
        every { InetAddress.getAllByName(legitimateHostname) } returns blockedIpAddress
        
        // Should be blocked because one of the resolved IPs is in denylist (post-DNS check)
        assertEquals(true, isHostInDenylist("https://$legitimateHostname", hostDenyList))
    }

    @Test
    fun `test hostname allowed when neither hostname nor resolved IPs are in denylist`() {
        // Test that a safe hostname resolving to safe IPs is allowed
        val safeHostname = "safe-domain.com"
        
        // Mock DNS resolution to return safe IPs only
        mockkStatic(InetAddress::class)
        val safeIpAddresses = arrayOf(
            InetAddress.getByName("174.12.0.0"),
            InetAddress.getByName("8.8.8.8")
        )
        every { InetAddress.getAllByName(safeHostname) } returns safeIpAddresses
        
        // Should NOT be blocked (both pre and post DNS checks pass)
        assertEquals(false, isHostInDenylist("https://$safeHostname", hostDenyList))
    }

    @Test
    fun `test DNS rebinding attack prevention - hostname resolves to private IP`() {
        // Simulate DNS rebinding attack where attacker's domain resolves to private IP
        val attackerDomain = "attacker-controlled.com"
        
        mockkStatic(InetAddress::class)
        // Attacker's domain resolves to internal network IP
        val privateIpAddresses = arrayOf(
            InetAddress.getByName("192.168.1.100") // In denylist: 192.168.0.0/16
        )
        every { InetAddress.getAllByName(attackerDomain) } returns privateIpAddresses
        
        // Should be blocked (post-DNS check catches the private IP)
        assertEquals(true, isHostInDenylist("https://$attackerDomain", hostDenyList))
    }

    @Test
    fun `test hostname with multiple resolved IPs where one is blocked`() {
        // Test that if ANY resolved IP is in denylist, the hostname is blocked
        val hostname = "multi-ip-domain.com"
        
        mockkStatic(InetAddress::class)
        val mixedIpAddresses = arrayOf(
            InetAddress.getByName("174.12.0.0"),    // Safe
            InetAddress.getByName("8.8.8.8"),       // Safe
            InetAddress.getByName("10.0.0.50")      // BLOCKED: 10.0.0.0/8
        )
        every { InetAddress.getAllByName(hostname) } returns mixedIpAddresses
        
        // Should be blocked because one resolved IP is in denylist
        assertEquals(true, isHostInDenylist("https://$hostname", hostDenyList))
    }

    @Test
    fun `test exact IP match in denylist is blocked`() {
        // Test that exact IP in denylist is blocked (e.g., 9.9.9.9)
        val exactBlockedIp = "9.9.9.9"
        
        // Should be blocked (exact IP match in denylist)
        assertEquals(true, isHostInDenylist("https://$exactBlockedIp", hostDenyList))
    }

    @Test
    fun `test CIDR range matching for denylist`() {
        // Test various IPs within CIDR ranges
        val testCases = mapOf(
            "10.255.255.255" to true,    // 10.0.0.0/8
            "172.31.255.255" to true,    // 172.16.0.0/12
            "192.168.255.255" to true,   // 192.168.0.0/16
            "127.255.255.255" to true,   // 127.0.0.0/8
            "172.15.0.1" to false,       // Outside 172.16.0.0/12
            "172.32.0.1" to false,       // Outside 172.16.0.0/12
            "11.0.0.1" to false          // Outside 10.0.0.0/8
        )
        
        testCases.forEach { (ip, shouldBeBlocked) ->
            assertEquals(
                shouldBeBlocked, 
                isHostInDenylist("https://$ip", hostDenyList),
                "IP $ip should ${if (shouldBeBlocked) "be blocked" else "not be blocked"}"
            )
        }
    }

    @Test
    fun `test DNS resolution failure handling`() {
        // Test that DNS resolution errors are handled gracefully
        val unresolveableHostname = "definitely-does-not-exist-12345.com"
        
        mockkStatic(InetAddress::class)
        every { InetAddress.getAllByName(unresolveableHostname) } throws java.net.UnknownHostException()
        
        // When DNS fails, getResolvedIps returns empty list, 
        // and isHostInDenylist should return false (only hostname check is done)
        assertEquals(false, isHostInDenylist("https://$unresolveableHostname", hostDenyList))
    }

    @Test
    fun `test hostname in denylist is blocked even if DNS resolution fails`() {
        // Test that if hostname is in denylist, it's blocked regardless of DNS issues
        val blockedHostname = "blocked-domain.com"
        val hostDenyListWithHostname = hostDenyList + listOf(blockedHostname)
        
        mockkStatic(InetAddress::class)
        every { InetAddress.getAllByName(blockedHostname) } throws java.net.UnknownHostException()
        
        // Should be blocked by pre-DNS check even though DNS resolution fails
        assertEquals(true, isHostInDenylist("https://$blockedHostname", hostDenyListWithHostname))
    }

    @Test
    fun `test localhost variations are blocked`() {
        // Test various localhost representations
        val localhostVariations = listOf(
            "127.0.0.1",
            "127.0.0.2",
            "127.1.1.1",
            "127.255.255.254"
        )
        
        localhostVariations.forEach { localhost ->
            assertEquals(
                true,
                isHostInDenylist("https://$localhost", hostDenyList),
                "Localhost variation $localhost should be blocked"
            )
        }
    }

    @Test
    fun `test validateUrlHost integration with denylist checks`() {
        // Test the validateUrlHost function that uses isHostInDenylist
        val blockedIp = "127.0.0.1"
        
        // Should throw exception because IP is in denylist
        val exception = assertThrows<IllegalArgumentException> {
            org.opensearch.notifications.core.utils.validateUrlHost("https://$blockedIp", hostDenyList)
        }
        
        assertEquals(
            "Host of url is denied, based on plugin setting [notification.core.http.host_deny_list]",
            exception.message
        )
    }

    @Test
    fun `test validateUrlHost with safe hostname passes both checks`() {
        val safeHostname = "safe-domain.com"
        
        mockkStatic(InetAddress::class)
        val safeIpAddresses = arrayOf(InetAddress.getByName("174.12.0.0"))
        every { InetAddress.getAllByName(safeHostname) } returns safeIpAddresses
        
        // Should not throw exception
        org.opensearch.notifications.core.utils.validateUrlHost("https://$safeHostname", hostDenyList)
    }
}
