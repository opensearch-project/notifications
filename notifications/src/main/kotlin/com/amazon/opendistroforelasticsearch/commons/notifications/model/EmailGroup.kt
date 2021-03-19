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

import com.amazon.opendistroforelasticsearch.notifications.util.isValidEmail
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import com.amazon.opendistroforelasticsearch.notifications.util.stringList
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing Email group.
 */
data class EmailGroup(
    val recipients: List<String>
) : Writeable, ToXContent {

    init {
        recipients.forEach {
            require(isValidEmail(it)) { "Invalid email address" }
        }
    }

    companion object {
        private val log by logger(EmailGroup::class.java)
        private const val RECIPIENTS_TAG = "recipients"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { EmailGroup(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): EmailGroup {
            var recipients: List<String>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    RECIPIENTS_TAG -> recipients = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing EmailGroup")
                    }
                }
            }
            recipients ?: throw IllegalArgumentException("$RECIPIENTS_TAG field absent")
            return EmailGroup(recipients)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        recipients = input.readStringList()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeStringCollection(recipients)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(RECIPIENTS_TAG, recipients)
            .endObject()
    }
}
