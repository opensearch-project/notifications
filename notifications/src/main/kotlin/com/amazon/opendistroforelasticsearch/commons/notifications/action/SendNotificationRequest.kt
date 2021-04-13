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
package com.amazon.opendistroforelasticsearch.commons.notifications.action

import com.amazon.opendistroforelasticsearch.commons.notifications.model.ChannelMessage
import com.amazon.opendistroforelasticsearch.commons.notifications.model.NotificationInfo
import com.amazon.opendistroforelasticsearch.commons.utils.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.commons.utils.logger
import com.amazon.opendistroforelasticsearch.commons.utils.stringList
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.action.ValidateActions
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class SendNotificationRequest : ActionRequest, ToXContentObject {
    val notificationInfo: NotificationInfo
    val channelMessage: ChannelMessage
    val channelIds: List<String>
    val threadContext: String?

    companion object {
        private val log by logger(SendNotificationRequest::class.java)
        private const val NOTIFICATION_INFO_TAG = "notificationInfo"
        private const val CHANNEL_MESSAGE_TAG = "channelMessage"
        private const val CHANNEL_IDS_TAG = "channelIds"
        private const val THREAD_CONTEXT_TAG = "context"

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
            var notificationInfo: NotificationInfo? = null
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
                    NOTIFICATION_INFO_TAG -> notificationInfo = NotificationInfo.parse(parser)
                    CHANNEL_MESSAGE_TAG -> channelMessage = ChannelMessage.parse(parser)
                    CHANNEL_IDS_TAG -> channelIds = parser.stringList()
                    THREAD_CONTEXT_TAG -> threadContext = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SendNotificationRequest")
                    }
                }
            }
            notificationInfo ?: throw IllegalArgumentException("$NOTIFICATION_INFO_TAG field absent")
            channelMessage ?: throw IllegalArgumentException("$CHANNEL_MESSAGE_TAG field absent")
            channelIds ?: throw IllegalArgumentException("$CHANNEL_IDS_TAG field absent")
            return SendNotificationRequest(notificationInfo, channelMessage, channelIds, threadContext)
        }
    }

    /**
     * constructor for creating the class
     * @param notificationInfo the notification info
     * @param channelMessage the message to be sent to channel
     * @param channelIds the ids of the notification configuration channel
     * @param threadContext the user info thread context
     */
    constructor(
        notificationInfo: NotificationInfo,
        channelMessage: ChannelMessage,
        channelIds: List<String>,
        threadContext: String?
    ) {
        this.notificationInfo = notificationInfo
        this.channelMessage = channelMessage
        this.channelIds = channelIds
        this.threadContext = threadContext
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        notificationInfo = NotificationInfo.reader.read(input)
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
        notificationInfo.writeTo(output)
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
            .field(NOTIFICATION_INFO_TAG, notificationInfo)
            .field(CHANNEL_MESSAGE_TAG, channelMessage)
            .field(CHANNEL_IDS_TAG, channelIds)
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
