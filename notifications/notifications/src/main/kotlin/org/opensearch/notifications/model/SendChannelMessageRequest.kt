/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.DocRequest
import org.opensearch.action.ValidateActions
import org.opensearch.commons.notifications.NotificationConstants.CHANNEL_MESSAGE_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_SOURCE_TAG
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import org.opensearch.notifications.NotificationsResourceSharingExtension.Companion.RESOURCE_TYPE
import org.opensearch.notifications.index.NotificationConfigIndex
import java.io.IOException

/**
 * Action request to send a caller-supplied message to one existing channel.
 *
 * It names a single channel, so it is a [DocRequest]: with resource sharing enabled, the security plugin checks the
 * caller's access to that channel before the action runs.
 */
class SendChannelMessageRequest : ActionRequest, DocRequest, ToXContentObject {
    val configId: String
    val eventSource: EventSource
    val channelMessage: ChannelMessage

    companion object {
        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SendChannelMessageRequest(it) }

        /**
         * Creator used in REST communication. The channel comes from the path, so the body carries only the event
         * source and the message; any other field, such as a channel list or a user context, is rejected.
         * @param parser XContentParser to deserialize data from.
         * @param configId the id of the channel, taken from the request path.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser, configId: String): SendChannelMessageRequest {
            var eventSource: EventSource? = null
            var channelMessage: ChannelMessage? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    EVENT_SOURCE_TAG -> eventSource = EventSource.parse(parser)
                    CHANNEL_MESSAGE_TAG -> channelMessage = ChannelMessage.parse(parser)
                    else -> throw IllegalArgumentException("Unexpected field $fieldName")
                }
            }
            requireNotNull(eventSource) { "$EVENT_SOURCE_TAG is required" }
            requireNotNull(channelMessage) { "$CHANNEL_MESSAGE_TAG is required" }
            return SendChannelMessageRequest(configId, eventSource, channelMessage)
        }
    }

    /**
     * constructor for creating the class
     * @param configId the id of the channel to send to
     * @param eventSource the event source of the message
     * @param channelMessage the message to send
     */
    constructor(configId: String, eventSource: EventSource, channelMessage: ChannelMessage) {
        this.configId = configId
        this.eventSource = eventSource
        this.channelMessage = channelMessage
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configId = input.readString()
        eventSource = EventSource.reader.read(input)
        channelMessage = ChannelMessage.reader.read(input)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(configId)
        eventSource.writeTo(output)
        channelMessage.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        // The channel id travels in the request path, so the body holds only what parse reads back
        return builder.startObject()
            .field(EVENT_SOURCE_TAG, eventSource)
            .field(CHANNEL_MESSAGE_TAG, channelMessage)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (configId.isBlank()) {
            validationException = ValidateActions.addValidationError("config id is empty", validationException)
        }
        return validationException
    }

    /**
     * {@inheritDoc}
     */
    override fun index(): String = NotificationConfigIndex.INDEX_NAME

    /**
     * {@inheritDoc}
     */
    override fun id(): String = configId

    /**
     * {@inheritDoc}
     */
    override fun type(): String = RESOURCE_TYPE
}
