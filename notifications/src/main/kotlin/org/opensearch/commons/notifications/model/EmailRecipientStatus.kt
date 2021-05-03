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

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_DETAIL_TAG
import org.opensearch.commons.utils.isValidEmail
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * Data class representing Email Recipient Status.
 */
data class EmailRecipientStatus(
    val recipient: String,
    val deliveryStatus: DeliveryStatus
) : BaseModel {

    init {
        require(isValidEmail(recipient)) { "Invalid email address" }
    }

    companion object {
        private val log by logger(EmailRecipientStatus::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { EmailRecipientStatus(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): EmailRecipientStatus {
            var recipient: String? = null
            var deliveryStatus: DeliveryStatus? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    RECIPIENT_TAG -> recipient = parser.text()
                    STATUS_DETAIL_TAG -> deliveryStatus = DeliveryStatus.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Email Recipient Status")
                    }
                }
            }
            recipient ?: throw IllegalArgumentException("$RECIPIENT_TAG field absent")
            deliveryStatus ?: throw IllegalArgumentException("$STATUS_DETAIL_TAG field absent")
            return EmailRecipientStatus(recipient, deliveryStatus)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        recipient = input.readString(),
        deliveryStatus = DeliveryStatus.reader.read(input)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(recipient)
        deliveryStatus.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(RECIPIENT_TAG, recipient)
            .field(STATUS_DETAIL_TAG, deliveryStatus)
            .endObject()
    }
}
