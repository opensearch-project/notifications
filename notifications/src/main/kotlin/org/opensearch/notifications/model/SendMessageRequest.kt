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

package org.opensearch.notifications.model

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.utils.createJsonParser
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import java.io.IOException

/**
 * Data class for storing the send message request.
 */
internal class SendMessageRequest : ActionRequest, ToXContentObject {
    val refTag: String
    val recipients: List<String>
    val title: String
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
        title: String,
        channelMessage: ChannelMessage
    ) : super() {
        this.refTag = refTag
        this.recipients = recipients
        this.title = title
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
        this.title = title
        this.channelMessage = ChannelMessage(textDescription, htmlDescription, attachment)
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
            .field(TITLE_FIELD, title)
            .field(TEXT_DESCRIPTION_FIELD, channelMessage.textDescription)
            .fieldIfNotNull(HTML_DESCRIPTION_FIELD, channelMessage.htmlDescription)
            .fieldIfNotNull(ATTACHMENT_FIELD, channelMessage.attachment)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
