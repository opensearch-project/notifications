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

package com.amazon.opendistroforelasticsearch.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notifications.util.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
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
    ) : ToXContentObject {
        internal companion object {
            private val log by logger(Attachment::class.java)
            private const val FILE_NAME_FIELD = "fileName"
            private const val FILE_ENCODING_FIELD = "fileEncoding"
            private const val FILE_DATA_FIELD = "fileData"
            private const val FILE_CONTENT_TYPE_FIELD = "fileContentType"

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
                        FILE_NAME_FIELD -> fileName = parser.text()
                        FILE_ENCODING_FIELD -> fileEncoding = parser.text()
                        FILE_DATA_FIELD -> fileData = parser.text()
                        FILE_CONTENT_TYPE_FIELD -> fileContentType = parser.text()
                        else -> {
                            parser.skipChildren()
                            log.info("$LOG_PREFIX:Skipping Unknown field $dataType")
                        }
                    }
                }
                fileName ?: throw IllegalArgumentException("attachment:fileName not present")
                fileEncoding ?: throw IllegalArgumentException("attachment:fileEncoding not present")
                fileData ?: throw IllegalArgumentException("attachment:fileData not present")
                return Attachment(fileName, fileEncoding, fileData, fileContentType)
            }
        }

        override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
            return builder!!.startObject()
                .field(FILE_NAME_FIELD, fileName)
                .field(FILE_ENCODING_FIELD, fileEncoding)
                .field(FILE_DATA_FIELD, fileData)
                .fieldIfNotNull(FILE_CONTENT_TYPE_FIELD, fileContentType)
                .endObject()
        }
    }
}
