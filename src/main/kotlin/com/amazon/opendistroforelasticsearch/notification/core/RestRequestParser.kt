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

package com.amazon.opendistroforelasticsearch.notification.core

import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.elasticsearch.rest.RestRequest

object RestRequestParser {
    private val log = LogManager.getLogger(javaClass)

    fun parse(request: RestRequest): NotificationMessage {
        var type: String? = null
        var message: NotificationMessage? = null
        val contentParser = request.contentOrSourceParamParser()
        ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.nextToken(), contentParser::getTokenLocation)
        while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
            val fieldName = contentParser.currentName()
            contentParser.nextToken()
            when (fieldName) {
                "type" -> type = contentParser.text()
                "params" -> { message = parseNotificationMessage(contentParser) }
                else -> {
                    contentParser.skipChildren()
                }
            }
        }
        if (type != "notification") {
            throw IllegalArgumentException("Unsupported Request type:$type")
        }
        return message ?: throw IllegalArgumentException("Request params not present")
    }

    private fun parseNotificationMessage(contentParser: XContentParser): NotificationMessage? {
        var refTag: String? = null
        var title: String? = null
        var textDescription: String? = null
        var htmlDescription: String? = null
        var textData: String? = null
        var binaryData: String? = null
        val recipients: MutableList<String> = mutableListOf()
        ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.currentToken(), contentParser::getTokenLocation)
        while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
            val fieldName = contentParser.currentName()
            contentParser.nextToken()
            when (fieldName) {
                "refTag" -> refTag = contentParser.text()
                "recipients" -> {
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, contentParser.currentToken(), contentParser::getTokenLocation)
                    while (contentParser.nextToken() != XContentParser.Token.END_ARRAY) {
                        recipients.add(contentParser.text())
                    }
                }
                "title" -> title = contentParser.text()
                "description" -> {
                    ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.currentToken(), contentParser::getTokenLocation)
                    while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
                        val descriptionType = contentParser.currentName()
                        contentParser.nextToken()
                        when (descriptionType) {
                            "text" -> textDescription = contentParser.text()
                            "html" -> htmlDescription = contentParser.text()
                        }
                    }
                }
                "data" -> {
                    ensureExpectedToken(XContentParser.Token.START_OBJECT, contentParser.currentToken(), contentParser::getTokenLocation)
                    while (contentParser.nextToken() != XContentParser.Token.END_OBJECT) {
                        val dataType = contentParser.currentName()
                        contentParser.nextToken()
                        when (dataType) {
                            "base64" -> binaryData = contentParser.text()
                            "text" -> textData = contentParser.text()
                        }
                    }
                }
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
        if (textDescription == null && htmlDescription == null) {
            throw IllegalArgumentException("Both text and html description not present")
        }
        if (binaryData != null && textData != null) {
            throw IllegalArgumentException("Both text and binary data present")
        }
        return NotificationMessage(refTag, title, recipients.toList(), textDescription, htmlDescription, binaryData, textData)
    }
}
