/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.notifications.spi.utils

import inet.ipaddr.IPAddressString
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.opensearch.common.Strings
import java.net.URL

fun validateUrl(urlString: String, hostDenyList: List<String>) {
    require(!Strings.isNullOrEmpty(urlString)) { "url is null or empty" }
    require(isValidUrl(urlString)) { "Invalid URL or unsupported" }
    require(!isHostInDenylist(urlString, hostDenyList)) {
        "Host of url is denied, based on plugin setting [notification.spi.email.host_deny_list]"
    }
}

fun validateEmail(email: String) {
    require(!Strings.isNullOrEmpty(email)) { "FromAddress and recipient should be provided" }
    require(isValidEmail(email)) { "Invalid email address" }
}

fun isValidUrl(urlString: String): Boolean {
    val url = URL(urlString) // throws MalformedURLException if URL is invalid
    return ("https" == url.protocol) // Support only HTTPS. HTTP and other protocols not supported
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
