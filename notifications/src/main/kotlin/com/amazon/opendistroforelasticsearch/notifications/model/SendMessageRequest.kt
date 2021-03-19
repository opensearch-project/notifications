/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notifications.util.createJsonParser
import com.amazon.opendistroforelasticsearch.notifications.util.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import com.amazon.opendistroforelasticsearch.notifications.util.objectIfNotNull
import com.amazon.opendistroforelasticsearch.notifications.util.stringList
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class for storing the send message request.
 */
internal class SendMessageRequest : ActionRequest, ToXContentObject {
    val refTag: String
    val recipients: List<String>
    val channelMessage: ChannelMessage

    companion object {
        private val log by logger(SendMessageRequest::class.java)
        private const val REF_TAG_FIELD = "refTag"
        private const val RECIPIENTS_FIELD = "recipients"
        private const val TITLE_FIELD = "title"
        private const val TEXT_DESCRIPTION_FIELD = "textDescription"
        private const val HTML_DESCRIPTION_FIELD = "htmlDescription"
        private const val ATTACHMENT_FIELD = "attachment"
    }

    constructor(
        refTag: String,
        recipients: List<String>,
        channelMessage: ChannelMessage
    ) : super() {
        this.refTag = refTag
        this.recipients = recipients
        this.channelMessage = channelMessage
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var refTag: String? = null
        var title: String? = null
        var textDescription: String? = null
        var htmlDescription: String? = null
        var attachment: ChannelMessage.Attachment? = null
        var recipients: List<String> = listOf()
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                REF_TAG_FIELD -> refTag = parser.text()
                RECIPIENTS_FIELD -> recipients = parser.stringList()
                TITLE_FIELD -> title = parser.text()
                TEXT_DESCRIPTION_FIELD -> textDescription = parser.text()
                HTML_DESCRIPTION_FIELD -> htmlDescription = parser.text()
                ATTACHMENT_FIELD -> attachment = ChannelMessage.Attachment.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        refTag = refTag ?: "noRef"
        if (recipients.isEmpty()) {
            throw IllegalArgumentException("Empty recipient list")
        }
        title ?: throw IllegalArgumentException("$TITLE_FIELD field not present")
        textDescription ?: throw IllegalArgumentException("$TEXT_DESCRIPTION_FIELD not present")
        this.refTag = refTag
        this.recipients = recipients
        this.channelMessage = ChannelMessage(title, textDescription, htmlDescription, attachment)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        toXContent(XContentFactory.jsonBuilder(output), ToXContent.EMPTY_PARAMS)
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @return created XContentBuilder object
     */
    fun toXContent(): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REF_TAG_FIELD, refTag)
            .field(RECIPIENTS_FIELD, recipients)
            .field(TITLE_FIELD, channelMessage.title)
            .field(TEXT_DESCRIPTION_FIELD, channelMessage.textDescription)
            .fieldIfNotNull(HTML_DESCRIPTION_FIELD, channelMessage.htmlDescription)
            .objectIfNotNull(ATTACHMENT_FIELD, channelMessage.attachment)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
