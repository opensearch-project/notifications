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
import com.amazon.opendistroforelasticsearch.notifications.util.createJsonParser
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParser.Token
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.rest.RestStatus
import java.io.IOException

/**
 * data class for storing send message response.
 */
internal class SendMessageResponse : BaseResponse {
    val refTag: String
    val channelMessageResponseList: List<ChannelMessageResponse>

    companion object {
        private val log by logger(SendMessageResponse::class.java)
        private const val REF_TAG_FIELD = "refTag"
        private const val RECIPIENTS_TAG_FIELD = "recipients"

        /**
         * Parse the recipient response list from parser
         * @param parser data referenced at parser
         * @return created list of ReportInstance
         */
        private fun parseRecipientResponseList(parser: XContentParser): List<ChannelMessageResponse> {
            val retList: MutableList<ChannelMessageResponse> = mutableListOf()
            XContentParserUtils.ensureExpectedToken(Token.START_ARRAY, parser.currentToken(), parser)
            while (parser.nextToken() != Token.END_ARRAY) {
                retList.add(ChannelMessageResponse.parse(parser))
            }
            return retList
        }
    }

    constructor(refTag: String, channelMessageResponseList: List<ChannelMessageResponse>) : super() {
        this.refTag = refTag
        this.channelMessageResponseList = channelMessageResponseList
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var refTag: String? = null
        var channelMessageResponseList: List<ChannelMessageResponse>? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                REF_TAG_FIELD -> refTag = parser.text()
                RECIPIENTS_TAG_FIELD -> channelMessageResponseList = parseRecipientResponseList(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        refTag = refTag ?: "noRef"
        channelMessageResponseList ?: throw IllegalArgumentException("$RECIPIENTS_TAG_FIELD field absent")
        this.refTag = refTag
        this.channelMessageResponseList = channelMessageResponseList
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        toXContent(XContentFactory.jsonBuilder(output), ToXContent.EMPTY_PARAMS)
    }

    /**
     * {@inheritDoc}
     */
    override fun getStatus(): RestStatus {
        val resultList = channelMessageResponseList.map { it.statusCode }.distinct()
        return if (resultList.size == 1) {
            resultList[0]
        } else {
            RestStatus.MULTI_STATUS
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REF_TAG_FIELD, refTag)
            .field(RECIPIENTS_TAG_FIELD, channelMessageResponseList)
            .endObject()
    }
}
