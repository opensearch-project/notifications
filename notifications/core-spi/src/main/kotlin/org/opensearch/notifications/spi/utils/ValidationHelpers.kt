/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.utils

import inet.ipaddr.IPAddressString
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.opensearch.common.Strings
import java.net.URL

fun validateUrl(urlString: String) {
    require(!Strings.isNullOrEmpty(urlString)) { "url is null or empty" }
    require(isValidUrl(urlString)) { "Invalid URL or unsupported" }
}

fun validateEmail(email: String) {
    require(!Strings.isNullOrEmpty(email)) { "FromAddress and recipient should be provided" }
    require(isValidEmail(email)) { "Invalid email address" }
}

fun isFQDN(urlString: String): Boolean {
    var p: java.lang.Process? = null
    try {
        p = java.lang.Runtime.getRuntime()
                .exec("nslookup " + urlString) // check host is FQDN or not
        val out = StringBuilder()
        val br = java.io.BufferedReader(java.io.InputStreamReader(p.getInputStream()))
        var line: String? = null
        var previous: String? = null
        while (br.readLine().also { line = it } != null) {
            if (line != previous) {
                previous = line
                out.append(line).append('\n')
            }
        }
        if (p.waitFor() == 0) {
            p.destroy()
        }
        return !out.toString().contains("server can't find")
    } catch (e: java.io.IOException) {
        println(e.printStackTrace())
    } catch (e: java.lang.InterruptedException) {
        println(e.printStackTrace())
    }
    return false
}

fun isValidUrl(urlString: String): Boolean {
    val url = URL(urlString) // throws MalformedURLException if URL is invalid
    val index: Int = urlString.indexOf("//") + 2
    val s: String = urlString.substring(index)

    val flag = isFQDN(s)
    if (s.equals(url.host)) {
        return (("https" == url.protocol || "http" == url.protocol) && flag) // Support only http/https, other protocols not supported
    } else {
        return (("https" == url.protocol || "http" == url.protocol) && !flag)
    }
}

fun isHostInDenylist(urlString: String, hostDenyList: List<String>): Boolean {
    val url = URL(urlString)
    if (url.host != null) {
        val ipStr = IPAddressString(url.host)
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
