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
package org.opensearch.commons.notifications.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CHANNEL_ID_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.CHANNEL_MESSAGE_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_SOURCE_TAG
import org.opensearch.commons.notifications.NotificationConstants.THREAD_CONTEXT_TAG
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import java.io.IOException

/**
 * Action Request to send notification.
 */
class SendNotificationRequest : ActionRequest, ToXContentObject {
    val eventSource: EventSource
    val channelMessage: ChannelMessage
    val channelIds: List<String>
    val threadContext: String?

    companion object {
        private val log by logger(SendNotificationRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SendNotificationRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SendNotificationRequest {
            var eventSource: EventSource? = null
            var channelMessage: ChannelMessage? = null
            var channelIds: List<String>? = null
            var threadContext: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EVENT_SOURCE_TAG -> eventSource = EventSource.parse(parser)
                    CHANNEL_MESSAGE_TAG -> channelMessage = ChannelMessage.parse(parser)
                    CHANNEL_ID_LIST_TAG -> channelIds = parser.stringList()
                    THREAD_CONTEXT_TAG -> threadContext = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SendNotificationRequest")
                    }
                }
            }
            eventSource ?: throw IllegalArgumentException("$EVENT_SOURCE_TAG field absent")
            channelMessage ?: throw IllegalArgumentException("$CHANNEL_MESSAGE_TAG field absent")
            channelIds ?: throw IllegalArgumentException("$CHANNEL_ID_LIST_TAG field absent")
            return SendNotificationRequest(eventSource, channelMessage, channelIds, threadContext)
        }
    }

    /**
     * constructor for creating the class
     * @param eventSource the notification info
     * @param channelMessage the message to be sent to channel
     * @param channelIds the ids of the notification configuration channel
     * @param threadContext the user info thread context
     */
    constructor(
        eventSource: EventSource,
        channelMessage: ChannelMessage,
        channelIds: List<String>,
        threadContext: String?
    ) {
        this.eventSource = eventSource
        this.channelMessage = channelMessage
        this.channelIds = channelIds
        this.threadContext = threadContext
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        eventSource = EventSource.reader.read(input)
        channelMessage = ChannelMessage.reader.read(input)
        channelIds = input.readStringList()
        threadContext = input.readOptionalString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        eventSource.writeTo(output)
        channelMessage.writeTo(output)
        output.writeStringCollection(channelIds)
        output.writeOptionalString(threadContext)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EVENT_SOURCE_TAG, eventSource)
            .field(CHANNEL_MESSAGE_TAG, channelMessage)
            .field(CHANNEL_ID_LIST_TAG, channelIds)
            .fieldIfNotNull(THREAD_CONTEXT_TAG, threadContext)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (channelIds.isEmpty()) {
            validationException = ValidateActions.addValidationError("channelIds is empty", validationException)
        }
        return validationException
    }
}
