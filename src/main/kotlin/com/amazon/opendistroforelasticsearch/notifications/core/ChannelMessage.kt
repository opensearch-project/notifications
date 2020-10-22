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

import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils

/**
 * Data class for storing channel message.
 */
internal data class ChannelMessage(
    val title: String,
    val textDescription: String,
    val htmlDescription: String?,
    val attachment: Attachment?
) {
    /**
     * Data class for storing attachment of channel message.
     */
    internal data class Attachment(
        val fileName: String,
        val fileEncoding: String,
        val fileData: String,
        val fileContentType: String?
    ) {
        internal companion object {
            /**
             * Parse the data from parser and create Attachment object
             * @param parser data referenced at parser
             * @return created Attachment object
             */
            fun parse(parser: XContentParser): Attachment {
                var fileName: String? = null
                var fileEncoding: String? = null
                var fileData: String? = null
                var fileContentType: String? = null
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation)
                while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                    val dataType = parser.currentName()
                    parser.nextToken()
                    when (dataType) {
                        "fileName" -> fileName = parser.text()
                        "fileEncoding" -> fileEncoding = parser.text()
                        "fileData" -> fileData = parser.text()
                        "fileContentType" -> fileContentType = parser.text()
                        else -> {
                            parser.skipChildren()
                        }
                    }
                }
                fileName ?: throw IllegalArgumentException("attachment:fileName not present")
                fileEncoding ?: throw IllegalArgumentException("attachment:fileEncoding not present")
                fileData ?: throw IllegalArgumentException("attachment:fileData not present")
                return Attachment(fileName, fileEncoding, fileData, fileContentType)
            }
        }
    }
}
