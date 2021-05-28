/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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
package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.HEADER_PARAMS_TAG
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.validateUrl
import java.io.IOException

/**
 * Data class representing Webhook channel.
 */
data class Webhook(
    val url: String,
    val headerParams: Map<String, String> = mapOf()
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(url)) { "URL is null or empty" }
        validateUrl(url)
    }

    companion object {
        private val log by logger(Webhook::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Webhook(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): Webhook {
            var url: String? = null
            var headerParams: Map<String, String> = mapOf()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    URL_TAG -> url = parser.text()
                    HEADER_PARAMS_TAG -> headerParams = parser.mapStrings()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Webhook destination")
                    }
                }
            }
            url ?: throw IllegalArgumentException("$URL_TAG field absent")
            return Webhook(url, headerParams)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(URL_TAG, url)
            .field(HEADER_PARAMS_TAG, headerParams)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        url = input.readString(),
        headerParams = input.readMap(STRING_READER, STRING_READER)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(url)
        output.writeMap(headerParams, STRING_WRITER, STRING_WRITER)
    }
}
