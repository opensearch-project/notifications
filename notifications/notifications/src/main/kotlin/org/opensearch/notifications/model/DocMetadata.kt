/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.time.Instant

/**
 * Class for storing document metadata that are not exposed to external entities.
 */
data class DocMetadata(
    val lastUpdateTime: Instant,
    val createdTime: Instant,
    val access: List<String>
) : ToXContent {
    companion object {
        private val log by logger(DocMetadata::class.java)
        const val METADATA_TAG = "metadata"
        const val ACCESS_LIST_TAG = "access"

        /**
         * Parse the data from parser and create object
         * @param parser data referenced at parser
         * @return created object
         */
        fun parse(parser: XContentParser): DocMetadata {
            var lastUpdateTime: Instant? = null
            var createdTime: Instant? = null
            var access: List<String> = listOf()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    UPDATED_TIME_TAG -> lastUpdateTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_TAG -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    ACCESS_LIST_TAG -> access = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("DocMetadata Skipping Unknown field $fieldName")
                    }
                }
            }
            lastUpdateTime ?: throw IllegalArgumentException("$UPDATED_TIME_TAG field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_TAG field absent")
            return DocMetadata(
                lastUpdateTime,
                createdTime,
                access
            )
        }
    }

    /**
     * {ref toXContent}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(UPDATED_TIME_TAG, lastUpdateTime.toEpochMilli())
            .field(CREATED_TIME_TAG, createdTime.toEpochMilli())
            .field(ACCESS_LIST_TAG, access)
            .endObject()
    }
}
