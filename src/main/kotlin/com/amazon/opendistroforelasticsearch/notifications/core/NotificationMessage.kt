/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.notifications.core

import com.amazon.opendistroforelasticsearch.notifications.util.stringList
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils

/**
 * Data class for storing the notification message.
 */
internal data class NotificationMessage(
    val refTag: String,
    val recipients: List<String>,
    val channelMessage: ChannelMessage
) {
    internal companion object {
        /**
         * Parse the data from parser and create Attachment object
         * @param parser data referenced at parser
         * @return created Attachment object
         */
        fun parse(parser: XContentParser): NotificationMessage {
            var refTag: String? = null
            var title: String? = null
            var textDescription: String? = null
            var htmlDescription: String? = null
            var attachment: ChannelMessage.Attachment? = null
            var recipients: List<String> = listOf()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation)
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    "refTag" -> refTag = parser.text()
                    "recipients" -> recipients = parser.stringList()
                    "title" -> title = parser.text()
                    "textDescription" -> textDescription = parser.text()
                    "htmlDescription" -> htmlDescription = parser.text()
                    "attachment" -> attachment = ChannelMessage.Attachment.parse(parser)
                    else -> {
                        parser.skipChildren()
                    }
                }
            }
            refTag = refTag ?: "noRef"
            if (recipients.isEmpty()) {
                throw IllegalArgumentException("Empty recipient list")
            }
            title ?: throw IllegalArgumentException("Title field not present")
            textDescription ?: throw IllegalArgumentException("textDescription not present")
            return NotificationMessage(refTag,
                recipients.toList(),
                ChannelMessage(title, textDescription, htmlDescription, attachment))
        }
    }
}
