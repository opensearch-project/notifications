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
import org.opensearch.commons.notifications.model.config.BaseConfigData
import org.opensearch.commons.utils.isValidEmail
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import java.io.IOException

/**
 * Data class representing Email account and default recipients.
 */
data class Email(
        val emailAccountID: String,
        val defaultRecipients: List<String>,
        val defaultEmailGroupIds: List<String>
) : BaseConfigData {

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
        fun parse(configDataMap: Map<String, Any>): Email {
            if (!configDataMap.containsKey(EMAIL_ACCOUNT_ID_TAG)) {
                throw IllegalArgumentException("${Email.EMAIL_ACCOUNT_ID_TAG} field absent")
            }

            val tempRecipients = configDataMap.getOrDefault(DEFAULT_RECIPIENTS_TAG, listOf<String>())
            var recipients: List<String> = listOf()
            if (tempRecipients is List<*>) {
                recipients = tempRecipients.filterIsInstance<String>()
            }


            var emailRecipients: List<String> = listOf()
            val tempEmailGroupIds = configDataMap.getOrDefault(DEFAULT_EMAIL_GROUPS_TAG, listOf<String>())
            if (tempEmailGroupIds is List<*>) {
                emailRecipients = tempEmailGroupIds.filterIsInstance<String>()
            }

            val emailAccountID: String = configDataMap.get(EMAIL_ACCOUNT_ID_TAG) as String
            return Email(emailAccountID, recipients, emailRecipients)
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
