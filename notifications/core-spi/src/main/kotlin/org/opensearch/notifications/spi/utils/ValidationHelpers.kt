/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.utils

import inet.ipaddr.HostName
import inet.ipaddr.IPAddressString
import org.apache.commons.validator.routines.DomainValidator
import org.apache.hc.client5.http.classic.methods.HttpPatch
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.logging.log4j.LogManager
import org.opensearch.core.common.Strings
import org.opensearch.notifications.spi.utils.ValidationHelpers.FQDN_REGEX
import java.lang.Exception
import java.net.InetAddress
import java.net.URL

private object ValidationHelpers {
    const val FQDN_REGEX =
        "^(?!.*?_.*?)(?!(?:\\w+?\\.)?-[\\w.\\-]*?)(?!\\w+?-\\.[\\w.\\-]+?)" +
            "(?=\\w)(?=[\\w.\\-]*?\\.+[\\w.\\-]*?)(?![\\w.\\-]{254})(?!(?" +
            ":\\.?[\\w\\-.]*?[\\w\\-]{64,}\\.)+?)[\\w.\\-]+?(?<![\\w\\-.]?\\." +
            "\\d?)(?<=[\\w\\-]{2,})(?<![\\w\\-]{25})\$"
}

fun validateUrl(urlString: String) {
    require(!Strings.isNullOrEmpty(urlString)) { "url is null or empty" }
    require(isValidUrl(urlString)) { "Invalid URL or unsupported" }
}

fun validateEmail(email: String) {
    require(!Strings.isNullOrEmpty(email)) { "FromAddress and recipient should be provided" }
    require(isValidEmail(email)) { "Invalid email address" }
}

fun isValidUrl(urlString: String): Boolean {
    val url = URL(urlString)

    val index: Int = urlString.indexOf("//") + 2
    val subString: String = urlString.substring(index)

    val regex = Regex(FQDN_REGEX)
    val isFQDN = regex.matches(subString)
    if (isFQDN && !DomainValidator.getInstance().isValid(subString)) return false
    return if (subString == url.host) {
        (("https" == url.protocol || "http" == url.protocol) && isFQDN)
    } else {
        (("https" == url.protocol || "http" == url.protocol) && !isFQDN)
    }
}

fun getResolvedIps(host: String): List<IPAddressString> {
    try {
        val resolvedIps = InetAddress.getAllByName(host)
        return resolvedIps.map { inetAddress -> IPAddressString(inetAddress.hostAddress) }
    } catch (e: Exception) {
        LogManager.getLogger().error("Unable to resolve host ips")
    }

    return listOf()
}

fun isHostInDenylist(
    urlString: String,
    hostDenyList: List<String>,
): Boolean {
    val url = URL(urlString)
    if (url.host != null) {
        val hostIpAddress = IPAddressString(url.host)
        val hostStr = HostName(url.host)

        // Parse denylist entries once
        val denyNetworks = hostDenyList.map { IPAddressString(it) }
        val denyHostnames = hostDenyList.map { HostName(it) }

        // FIRST CHECK: Before DNS resolution
        // Check hostname match
        for (denyHostStr in denyHostnames) {
            if (denyHostStr.equals(hostStr)) {
                LogManager.getLogger().error("$url.host is denied (hostname in denylist)")
                return true
            }
        }

        // Check direct IP with IPv4/IPv6 compatibility
        if (hostIpAddress.isValid) {
            val hostAddr = hostIpAddress.address
            if (hostAddr != null && isIpInDenylist(hostAddr, denyNetworks, url.host)) {
                return true
            }
        }

        // SECOND CHECK: After DNS resolution
        // Resolve hostname and check all resolved IPs against denylist
        val resolvedIpStrings = getResolvedIps(url.host)

        for (ipStr in resolvedIpStrings) {
            val resolvedAddr = ipStr.address
            if (resolvedAddr != null && isIpInDenylist(resolvedAddr, denyNetworks, url.host)) {
                return true
            }
        }
    }

    return false
}

/**
 * Check if an IP address (or its IPv4/IPv6 variants) is in the denylist
 * Handles IPv4-mapped IPv6 addresses (::ffff:a.b.c.d) and IPv4-compatible IPv6 addresses
 */
private fun isIpInDenylist(
    ip: inet.ipaddr.IPAddress,
    denyNetworks: List<IPAddressString>,
    host: String,
): Boolean {
    val candidates = mutableListOf(ip)

    // IPv4 -> add IPv6-mapped version (::ffff:a.b.c.d)
    if (ip.isIPv4) {
        try {
            val ipv6Mapped = IPAddressString("::ffff:${ip.toNormalizedString()}").address
            if (ipv6Mapped != null) {
                candidates.add(ipv6Mapped)
            }
        } catch (e: Exception) {
            LogManager.getLogger().debug("Failed to create IPv6-mapped address for $ip")
        }
    }

    // IPv6 -> extract IPv4 if it's IPv4-mapped or IPv4-compatible
    if (ip.isIPv6) {
        try {
            val ipv6Address = ip.toIPv6()

            // Check for IPv4-mapped IPv6 (::ffff:a.b.c.d)
            if (ipv6Address.isIPv4Mapped) {
                val ipv4 = ipv6Address.embeddedIPv4Address
                if (ipv4 != null) {
                    candidates.add(ipv4)
                }
            }

            // Check for IPv4-compatible IPv6 (::a.b.c.d)
            if (ipv6Address.isIPv4Compatible) {
                val ipv4 = ipv6Address.embeddedIPv4Address
                if (ipv4 != null) {
                    candidates.add(ipv4)
                }
            }
        } catch (e: Exception) {
            LogManager.getLogger().debug("Failed to extract IPv4 from IPv6 address $ip")
        }
    }

    // Check all candidate addresses against denylist
    for (candidate in candidates) {
        for (denyNetwork in denyNetworks) {
            val denyAddr = denyNetwork.address
            if (denyAddr != null) {
                // Handle unspecified addresses (0.0.0.0 or ::)
                if ((candidate.isZero && denyAddr.isZero) || denyAddr.contains(candidate)) {
                    LogManager.getLogger().error(
                        "$host is denied by rule ${denyNetwork.toNormalizedString()} " +
                            "(matched ${candidate.toNormalizedString()})",
                    )
                    return true
                }
            }
        }
    }

    return false
}

/**
 * RFC 5322 compliant pattern matching: https://www.ietf.org/rfc/rfc5322.txt
 * Regex was based off of this post: https://stackoverflow.com/a/201378
 */
fun isValidEmail(email: String): Boolean {
    val validEmailPattern =
        Regex(
            "(?:[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+)*" +
                "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]" + "" +
                "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" +
                "|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}" +
                "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]" + "" +
                "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
            RegexOption.IGNORE_CASE,
        )
    return validEmailPattern.matches(email)
}

fun validateMethod(method: String) {
    require(!Strings.isNullOrEmpty(method)) { "Method is null or empty" }
    val validMethods = listOf(HttpPost.METHOD_NAME, HttpPut.METHOD_NAME, HttpPatch.METHOD_NAME)
    require(
        method.findAnyOf(validMethods) != null,
    ) { "Invalid method supplied. Only POST, PUT and PATCH are allowed" }
}
