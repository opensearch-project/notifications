/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model

import org.opensearch.core.common.Strings

/**
 *  class for storing message.
 */
@SuppressWarnings("LongParameterList")
class MessageContent(
    val title: String,
    val textDescription: String,
    val htmlDescription: String? = null,
    val fileName: String? = null,
    val fileEncoding: String? = null,
    val fileData: String? = null,
    val fileContentType: String? = null
) {

    init {
        require(!Strings.isNullOrEmpty(title)) { "title is null or empty" }
        require(!Strings.isNullOrEmpty(textDescription) || !Strings.isNullOrEmpty(htmlDescription)) {
            "both text message part and html message part are null or empty"
        }
    }

    fun buildMessageWithTitle(): String {
        return if (!Strings.isNullOrEmpty(htmlDescription)) {
            htmlDescription!!
        } else {
            "$title\n\n$textDescription"
        }
    }
}
