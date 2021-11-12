/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.EVENT_TAG
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG
import java.io.IOException

/**
 * Data class representing Notification event with metadata.
 */
data class NotificationEventDoc(
    val metadata: DocMetadata,
    val event: NotificationEvent
) : ToXContent {

    companion object {
        private val log by logger(NotificationEventDoc::class.java)

        /**
         * Parse the data from parser and create object
         * @param parser data referenced at parser
         * @return created object
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationEventDoc {
            var metadata: DocMetadata? = null
            var event: NotificationEvent? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    METADATA_TAG -> metadata = DocMetadata.parse(parser)
                    EVENT_TAG -> event = NotificationEvent.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing event doc")
                    }
                }
            }
            metadata ?: throw IllegalArgumentException("$METADATA_TAG field absent")
            event ?: throw IllegalArgumentException("$EVENT_TAG field absent")
            return NotificationEventDoc(
                metadata,
                event
            )
        }
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(METADATA_TAG, metadata)
            .field(EVENT_TAG, event)
            .endObject()
    }
}
