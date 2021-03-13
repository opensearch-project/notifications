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
package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils

/**
 * Data class representing Delivery Status.
 */
data class DeliveryStatus(
    val statusCode: String,
    val statusText: String
) : Writeable, ToXContent {

    init {
        require(!Strings.isNullOrEmpty(statusCode)) { "StatusCode is null or empty" }
        require(!Strings.isNullOrEmpty(statusText)) { "statusText is null or empty" }
    }
    companion object {
        private val log by logger(DeliveryStatus::class.java)
        private const val STATUS_CODE_TAG = "statusCode"
        private const val STATUS_TEXT_TAG = "statusText"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { DeliveryStatus(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        fun parse(parser: XContentParser): DeliveryStatus {
            var statusCode: String? = null
            var statusText: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser::getTokenLocation
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    STATUS_CODE_TAG -> statusCode = parser.text()
                    STATUS_TEXT_TAG -> statusText = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing deliveryStatus")
                    }
                }
            }
            statusCode ?: throw IllegalArgumentException("$STATUS_CODE_TAG field absent")
            statusText ?: throw IllegalArgumentException("$STATUS_TEXT_TAG field absent")
            return DeliveryStatus(
                statusCode,
                statusText
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        statusCode = input.readString(),
        statusText = input.readString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(statusCode)
        output.writeString(statusText)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(STATUS_CODE_TAG, statusCode)
            .field(STATUS_TEXT_TAG, statusText)
            .endObject()
    }
}
