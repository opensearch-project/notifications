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
package com.amazon.opensearch.commons.notifications.model

import com.amazon.opensearch.commons.utils.logger
import com.amazon.opensearch.commons.utils.stringList
import com.amazon.opensearch.commons.utils.valueOf
import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing Notification.
 */
data class NotificationInfo(
    val title: String,
    val referenceId: String,
    val feature: Feature,
    val severity: SeverityType = SeverityType.Info,
    val tags: List<String> = listOf()
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(title)) { "name is null or empty" }
    }

    companion object {
        private val log by logger(NotificationInfo::class.java)
        private const val TITLE_TAG = "title"
        private const val REFERENCE_ID_TAG = "referenceId"
        private const val FEATURE_TAG = "feature"
        private const val SEVERITY_TAG = "severity"
        private const val TAGS_TAG = "tags"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationInfo(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationInfo {
            var title: String? = null
            var referenceId: String? = null
            var feature: Feature? = null
            var severity: SeverityType = SeverityType.Info
            var tags: List<String> = emptyList()

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
                    FEATURE_TAG -> feature = valueOf(parser.text(), Feature.None, log)
                    SEVERITY_TAG -> severity = valueOf(parser.text(), SeverityType.None, log)
                    TAGS_TAG -> tags = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing NotificationInfo")
                    }
                }
            }
            title ?: throw IllegalArgumentException("$TITLE_TAG field absent")
            referenceId ?: throw IllegalArgumentException("$REFERENCE_ID_TAG field absent")
            feature ?: throw IllegalArgumentException("$FEATURE_TAG field absent")

            return NotificationInfo(
                title,
                referenceId,
                feature,
                severity,
                tags
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
        feature = input.readEnum(Feature::class.java),
        severity = input.readEnum(SeverityType::class.java),
        tags = input.readStringList()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(title)
        output.writeString(referenceId)
        output.writeEnum(feature)
        output.writeEnum(severity)
        output.writeStringCollection(tags)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(TITLE_TAG, title)
            .field(REFERENCE_ID_TAG, referenceId)
            .field(FEATURE_TAG, feature)
            .field(SEVERITY_TAG, severity)
            .field(TAGS_TAG, tags)
            .endObject()
    }
}
