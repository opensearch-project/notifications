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
package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.util.logger
import com.amazon.opendistroforelasticsearch.notifications.util.objectList
import com.amazon.opendistroforelasticsearch.notifications.util.stringList
import com.amazon.opendistroforelasticsearch.notifications.util.valueOf
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing Notification.
 */
data class Notification(
    val title: String,
    val referenceId: String,
    val source: SourceType,
    val severity: SeverityType,
    val tags: List<String> = listOf(),
    val statusList: List<NotificationStatus> = listOf()
) : Writeable, ToXContent {

    init {
        require(!Strings.isNullOrEmpty(title)) { "name is null or empty" }
    }

    enum class SourceType { None, Alerting, IndexManagement, Reports }
    enum class SeverityType { None, High, Info, Critical }

    companion object {
        private val log by logger(Notification::class.java)
        private const val TITLE_TAG = "title"
        private const val REFERENCE_ID_TAG = "referenceId"
        private const val SOURCE_TAG = "source"
        private const val SEVERITY_TAG = "severity"
        private const val TAGS_TAG = "tags"
        private const val STATUS_LIST_TAG = "statusList"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Notification(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @Suppress("ComplexMethod")
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): Notification {
            var title: String? = null
            var referenceId: String? = null
            var source: SourceType? = null
            var severity: SeverityType = SeverityType.Info
            var tags: List<String> = emptyList()
            var statusList: List<NotificationStatus> = listOf()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    TITLE_TAG -> title = parser.text()
                    REFERENCE_ID_TAG -> referenceId = parser.text()
                    SOURCE_TAG -> source = valueOf(parser.text(), SourceType.None)
                    SEVERITY_TAG -> severity = valueOf(parser.text(), SeverityType.None)
                    TAGS_TAG -> tags = parser.stringList()
                    STATUS_LIST_TAG -> statusList = parser.objectList { NotificationStatus.parse(it) }
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing notification")
                    }
                }
            }
            title ?: throw IllegalArgumentException("$TITLE_TAG field absent")
            referenceId ?: throw IllegalArgumentException("$REFERENCE_ID_TAG field absent")
            source ?: throw IllegalArgumentException("$SOURCE_TAG field absent")

            return Notification(
                title,
                referenceId,
                source,
                severity,
                tags,
                statusList
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        title = input.readString(),
        referenceId = input.readString(),
        source = input.readEnum(SourceType::class.java),
        severity = input.readEnum(SeverityType::class.java),
        tags = input.readStringList(),
        statusList = input.readList(NotificationStatus.reader)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(title)
        output.writeString(referenceId)
        output.writeEnum(source)
        output.writeEnum(severity)
        output.writeStringCollection(tags)
        output.writeList(statusList)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(TITLE_TAG, title)
            .field(REFERENCE_ID_TAG, referenceId)
            .field(SOURCE_TAG, source)
            .field(SEVERITY_TAG, severity)
            .field(TAGS_TAG, tags)
            .field(STATUS_LIST_TAG, statusList)
            .endObject()
    }
}
