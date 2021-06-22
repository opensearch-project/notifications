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

package org.opensearch.notifications.spi.utils

import java.net.URL

// Valid ID characters = (All Base64 chars + "_-") to support UUID format and Base64 encoded IDs
private val VALID_ID_CHARS: Set<Char> = (('a'..'z') + ('A'..'Z') + ('0'..'9') + '+' + '/' + '_' + '-').toSet()

fun validateUrl(urlString: String) {
    require(isValidUrl(urlString)) { "Invalid URL or unsupported" }
    val url = URL(urlString)
    require("https" == url.protocol) // Support only HTTPS. HTTP and other protocols not supported
    // TODO : Add hosts deny list
}

fun validateEmail(email: String) {
    require(isValidEmail(email)) { "Invalid email address" }
}

fun validateId(idString: String) {
    require(isValidId(idString)) { "Invalid characters in id : ${idString.filterNot { VALID_ID_CHARS.contains(it) }}" }
}

fun isValidUrl(urlString: String): Boolean {
    val url = URL(urlString) // throws MalformedURLException if URL is invalid
    // TODO : Add hosts deny list
    return ("https" == url.protocol) // Support only HTTPS. HTTP and other protocols not supported
}

/**
 * RFC 5322 compliant pattern matching: https://www.ietf.org/rfc/rfc5322.txt
 * Regex was based off of this post: https://stackoverflow.com/a/201378
 */
fun isValidEmail(email: String): Boolean {
    val validEmailPattern = Regex(
        "(?:[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+\\/=?^_`{|}~-]+)*" +
                "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" +
                "|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}" +
                "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
        RegexOption.IGNORE_CASE
    )
    return validEmailPattern.matches(email)
}

fun isValidId(idString: String): Boolean {
    return idString.isNotBlank() && idString.all { VALID_ID_CHARS.contains(it) }
}
