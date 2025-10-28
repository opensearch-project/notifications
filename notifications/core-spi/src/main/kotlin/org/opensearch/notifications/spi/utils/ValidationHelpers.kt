/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.utils

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
        (("https" == url.protocol || "http" == url.protocol) && isFQDN) // Support only http/https, other protocols not supported
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

fun isHostInDenylist(urlString: String, hostDenyList: List<String>): Boolean {
    val url = URL(urlString)
    if (url.host != null) {
        // FIRST CHECK: If URL uses direct IP (not hostname), check immediately
        val hostIpAddress = IPAddressString(url.host)

        if (hostIpAddress.isValid) {
            // It's a direct IP address - check against denylist before DNS
            for (network in hostDenyList) {
                val denyIpStr = IPAddressString(network)
                if (denyIpStr.contains(hostIpAddress)) {
                    LogManager.getLogger().error("${url.host} is denied (direct IP in denylist)")
                    return true
                }
            }
        }

        // SECOND CHECK: Resolve hostname to IPs and check those
        val resolvedIpStrings = getResolvedIps(url.host)

        for (network in hostDenyList) {
            val denyIpStr = IPAddressString(network)

            for (ipStr in resolvedIpStrings) {
                if (denyIpStr.contains(ipStr)) {
                    LogManager.getLogger().error("${url.host} resolved to $ipStr which is denied")
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
    val validEmailPattern = Regex(
        "(?:[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+)*" +
            "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]" + "" +
            "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" +
            "|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}" +
            "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]" + "" +
            "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
        RegexOption.IGNORE_CASE
    )
    return validEmailPattern.matches(email)
}

fun validateMethod(method: String) {
    require(!Strings.isNullOrEmpty(method)) { "Method is null or empty" }
    val validMethods = listOf(HttpPost.METHOD_NAME, HttpPut.METHOD_NAME, HttpPatch.METHOD_NAME)
    require(
        method.findAnyOf(validMethods) != null
    ) { "Invalid method supplied. Only POST, PUT and PATCH are allowed" }
}
