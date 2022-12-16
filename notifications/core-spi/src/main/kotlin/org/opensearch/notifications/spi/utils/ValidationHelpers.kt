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
import org.opensearch.common.Strings
import org.opensearch.notifications.spi.utils.ValidationHelpers.FQDN_REGEX
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

fun isHostInDenylist(urlString: String, hostDenyList: List<String>): Boolean {
    val url = URL(urlString)
    if (url.host != null) {
        val ipStr = IPAddressString(InetAddress.getByName(url.host).hostAddress)
        for (network in hostDenyList) {
            val netStr = IPAddressString(network)
            if (netStr.contains(ipStr)) {
                return true
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
