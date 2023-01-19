/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model

import org.opensearch.common.Strings

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
        require(!Strings.isNullOrEmpty(textDescription)) { "text message part is null or empty" }
        require(!Strings.isNullOrEmpty(htmlDescription)) { "html message part is null or empty" }
        if (fileName != null) {
            require(!Strings.isNullOrEmpty(fileName)) { "file name is null or empty" }
            require(!Strings.isNullOrEmpty(fileEncoding)) { "file encoding is null or empty" }
            require(!Strings.isNullOrEmpty(fileData)) { "file data is null or empty" }
            require(!Strings.isNullOrEmpty(fileContentType)) { "file content type is null or empty" }
        }
    }

    fun buildMessageWithTitle(): String {
        return "$title\n\n$textDescription\n\n$htmlDescription"
    }
}
