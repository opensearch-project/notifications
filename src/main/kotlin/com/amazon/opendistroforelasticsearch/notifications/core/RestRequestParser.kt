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

package com.amazon.opendistroforelasticsearch.notifications.core

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.elasticsearch.rest.RestRequest

/**
 * This object parses the Rest request for notification plugin.
 */
object RestRequestParser {
    private val log = LogManager.getLogger(javaClass)

    /**
     * Parses request and returns object data.
     *
     * @param request the rest request to parse
     * @return parsed object [NotificationMessage]
     */
    fun parse(request: RestRequest): NotificationMessage {
        log.debug("${NotificationPlugin.PLUGIN_NAME}:RestRequestParser")
        val contentParser = request.contentOrSourceParamParser()
        contentParser.nextToken()
        return parseNotificationMessage(contentParser)
    }

    /**
     * Parses request and returns object data.
     *
     * @param contentParser opened content parser
     * @return parsed object [NotificationMessage]
     */
    private fun parseNotificationMessage(contentParser: XContentParser): NotificationMessage {
        var refTag: String? = null
        var title: String? = null
        var textDescription: String? = null
        var htmlDescription: String? = null
        var attachment: ChannelMessage.Attachment? = null
        val recipients: MutableList<String> = mutableListOf()
        ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.currentToken(), contentParser::getTokenLocation)
        while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
            val fieldName = contentParser.currentName()
            contentParser.nextToken()
            when (fieldName) {
                "refTag" -> refTag = contentParser.text()
                "recipients" -> {
                    parseRecipients(contentParser, recipients)
                }
                "title" -> title = contentParser.text()
                "textDescription" -> textDescription = contentParser.text()
                "htmlDescription" -> htmlDescription = contentParser.text()
                "attachment" -> attachment = parseAttachment(contentParser)
                else -> {
                    contentParser.skipChildren()
                }
            }
        }
        refTag = refTag ?: "noRef"
        if (recipients.isEmpty()) {
            throw IllegalArgumentException("Empty recipient list")
        }
        title ?: throw IllegalArgumentException("Title field not present")
        textDescription ?: throw IllegalArgumentException("textDescription not present")
        return NotificationMessage(refTag,
            recipients.toList(),
            ChannelMessage(title, textDescription, htmlDescription, attachment))
    }

    /**
     * Parse "recipients" section of json
     * @param contentParser opened content parser
     * @param recipients parsed recipients added to this mutable list
     */
    private fun parseRecipients(contentParser: XContentParser, recipients: MutableList<String>) {
        ensureExpectedToken(XContentParser.Token.START_ARRAY, contentParser.currentToken(), contentParser::getTokenLocation)
        while (contentParser.nextToken() != XContentParser.Token.END_ARRAY) {
            recipients.add(contentParser.text())
        }
    }

    /**
     * Parse "attachment" section of json
     * @param contentParser opened content parser
     * @return parsed [ChannelMessage.Attachment] object
     */
    private fun parseAttachment(contentParser: XContentParser): ChannelMessage.Attachment {
        var fileName: String? = null
        var fileEncoding: String? = null
        var fileData: String? = null
        var fileContentType: String? = null
        ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.currentToken(), contentParser::getTokenLocation)
        while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
            val dataType = contentParser.currentName()
            contentParser.nextToken()
            when (dataType) {
                "fileName" -> fileName = contentParser.text()
                "fileEncoding" -> fileEncoding = contentParser.text()
                "fileData" -> fileData = contentParser.text()
                "fileContentType" -> fileContentType = contentParser.text()
            }
        }
        fileName ?: throw IllegalArgumentException("attachment:fileName not present")
        fileEncoding ?: throw IllegalArgumentException("attachment:fileEncoding not present")
        fileData ?: throw IllegalArgumentException("attachment:fileData not present")
        return ChannelMessage.Attachment(fileName, fileEncoding, fileData, fileContentType)
    }
}
