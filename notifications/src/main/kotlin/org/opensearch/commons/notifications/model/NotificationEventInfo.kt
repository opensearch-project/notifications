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
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.TENANT_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.utils.logger
import java.io.IOException
import java.time.Instant

/**
 * Data class representing Notification event with information.
 */
data class NotificationEventInfo(
    val eventId: String,
    val lastUpdatedTime: Instant,
    val createdTime: Instant,
    val tenant: String,
    val notificationEvent: NotificationEvent
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(eventId)) { "event id is null or empty" }
    }

    companion object {
        private val log by logger(NotificationEventInfo::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationEventInfo(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationEventInfo {
            var eventId: String? = null
            var lastUpdatedTime: Instant? = null
            var createdTime: Instant? = null
            var tenant: String? = null
            var notificationEvent: NotificationEvent? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EVENT_ID_TAG -> eventId = parser.text()
                    UPDATED_TIME_TAG -> lastUpdatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_TAG -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    TENANT_TAG -> tenant = parser.text()
                    EVENT_TAG -> notificationEvent = NotificationEvent.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing event info")
                    }
                }
            }
            eventId ?: throw IllegalArgumentException("$EVENT_ID_TAG field absent")
            lastUpdatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_TAG field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_TAG field absent")
            tenant = tenant ?: ""
            notificationEvent ?: throw IllegalArgumentException("$EVENT_TAG field absent")
            return NotificationEventInfo(
                eventId,
                lastUpdatedTime,
                createdTime,
                tenant,
                notificationEvent
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EVENT_ID_TAG, eventId)
            .field(UPDATED_TIME_TAG, lastUpdatedTime.toEpochMilli())
            .field(CREATED_TIME_TAG, createdTime.toEpochMilli())
            .field(TENANT_TAG, tenant)
            .field(EVENT_TAG, notificationEvent)
            .endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        eventId = input.readString(),
        lastUpdatedTime = input.readInstant(),
        createdTime = input.readInstant(),
        tenant = input.readString(),
        notificationEvent = NotificationEvent.reader.read(input)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(eventId)
        output.writeInstant(lastUpdatedTime)
        output.writeInstant(createdTime)
        output.writeString(tenant)
        notificationEvent.writeTo(output)
    }
}
