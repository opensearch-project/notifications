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
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing Email account and default recipients.
 */
data class Email(
    val emailAccountID: String,
    val defaultRecipients: List<String>,
    val defaultEmailGroupIds: List<String>
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(emailAccountID)) { "emailAccountID is null or empty" }
        defaultRecipients.forEach {
            require(isValidEmail(it)) { "Invalid email address" }
        }
    }

    companion object {
        private val log by logger(Email::class.java)
        private const val EMAIL_ACCOUNT_ID_TAG = "emailAccountID"
        private const val DEFAULT_RECIPIENTS_TAG = "defaultRecipients"
        private const val DEFAULT_EMAIL_GROUPS_TAG = "defaultEmailGroupIds"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Email(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): Email {
            var emailAccountID: String? = null
            var recipients: List<String> = listOf()
            var emailGroupIds: List<String> = listOf()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EMAIL_ACCOUNT_ID_TAG -> emailAccountID = parser.text()
                    DEFAULT_RECIPIENTS_TAG -> recipients = parser.stringList()
                    DEFAULT_EMAIL_GROUPS_TAG -> emailGroupIds = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Email")
                    }
                }
            }
            emailAccountID ?: throw IllegalArgumentException("$EMAIL_ACCOUNT_ID_TAG field absent")
            return Email(emailAccountID, recipients, emailGroupIds)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        emailAccountID = input.readString(),
        defaultRecipients = input.readStringList(),
        defaultEmailGroupIds = input.readStringList()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(emailAccountID)
        output.writeStringCollection(defaultRecipients)
        output.writeStringCollection(defaultEmailGroupIds)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EMAIL_ACCOUNT_ID_TAG, emailAccountID)
            .field(DEFAULT_RECIPIENTS_TAG, defaultRecipients)
            .field(DEFAULT_EMAIL_GROUPS_TAG, defaultEmailGroupIds)
            .endObject()
    }
}
