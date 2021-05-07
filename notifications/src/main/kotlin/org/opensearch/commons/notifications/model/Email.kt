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
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_ACCOUNT_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_GROUP_ID_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_LIST_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.commons.utils.validateEmail
import java.io.IOException

/**
 * Data class representing Email account and default recipients.
 */
data class Email(
    val emailAccountID: String,
    val recipients: List<String>,
    val emailGroupIds: List<String>
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(emailAccountID)) { "emailAccountID is null or empty" }
        recipients.forEach {
            validateEmail(it)
        }
    }

    companion object {
        private val log by logger(Email::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Email(it) }

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
                    RECIPIENT_LIST_TAG -> recipients = parser.stringList()
                    EMAIL_GROUP_ID_LIST_TAG -> emailGroupIds = parser.stringList()
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
        recipients = input.readStringList(),
        emailGroupIds = input.readStringList()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(emailAccountID)
        output.writeStringCollection(recipients)
        output.writeStringCollection(emailGroupIds)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EMAIL_ACCOUNT_ID_TAG, emailAccountID)
            .field(RECIPIENT_LIST_TAG, recipients)
            .field(EMAIL_GROUP_ID_LIST_TAG, emailGroupIds)
            .endObject()
    }
}
