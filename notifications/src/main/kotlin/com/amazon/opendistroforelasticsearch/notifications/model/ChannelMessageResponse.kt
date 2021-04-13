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
import com.amazon.opendistroforelasticsearch.commons.utils.logger
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.rest.RestStatus

/**
 * Data class for storing channel message response per recipient.
 */
internal data class ChannelMessageResponse(
    val recipient: String,
    val statusCode: RestStatus,
    val statusText: String
) : ToXContentObject {
    internal companion object {
        private val log by logger(ChannelMessageResponse::class.java)
        private const val RECIPIENT_TAG = "recipient"
        private const val STATUS_CODE_TAG = "statusCode"
        private const val STATUS_TEXT_TAG = "statusText"

        /**
         * Parse the data from parser and create ChannelMessageResponse object
         * @param parser data referenced at parser
         * @return created ChannelMessageResponse object
         */
        fun parse(parser: XContentParser): ChannelMessageResponse {
            var recipient: String? = null
            var statusCode: RestStatus? = null
            var statusText: String? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    RECIPIENT_TAG -> recipient = parser.text()
                    STATUS_CODE_TAG -> statusCode = RestStatus.fromCode(parser.intValue())
                    STATUS_TEXT_TAG -> statusText = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:ChannelMessageResponse Skipping Unknown field $fieldName")
                    }
                }
            }
            recipient ?: throw IllegalArgumentException("$RECIPIENT_TAG field absent")
            statusCode ?: throw IllegalArgumentException("$STATUS_CODE_TAG field absent")
            statusText ?: throw IllegalArgumentException("$STATUS_TEXT_TAG field absent")
            return ChannelMessageResponse(recipient, statusCode, statusText)
        }
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @return created XContentBuilder object
     */
    fun toXContent(): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(RECIPIENT_TAG, recipient)
            .field(STATUS_CODE_TAG, statusCode.status)
            .field(STATUS_TEXT_TAG, statusText)
            .endObject()
        return builder
    }
}
