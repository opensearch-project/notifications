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
package com.amazon.opensearch.commons.notifications.action

import com.amazon.opensearch.commons.utils.logger
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class UpdateNotificationConfigResponse : com.amazon.opensearch.commons.notifications.action.BaseResponse {
    val configId: String

    companion object {
        private val log by logger(UpdateNotificationConfigResponse::class.java)
        private const val CONFIG_ID_TAG = "configId"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { UpdateNotificationConfigResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): UpdateNotificationConfigResponse {
            var configId: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_ID_TAG -> configId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing UpdateNotificationConfigResponse")
                    }
                }
            }
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            return UpdateNotificationConfigResponse(configId)
        }
    }

    /**
     * constructor for creating the class
     * @param configId the id of the updated notification configuration
     */
    constructor(configId: String) {
        this.configId = configId
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configId = input.readString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(configId)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_TAG, configId)
            .endObject()
    }
}
