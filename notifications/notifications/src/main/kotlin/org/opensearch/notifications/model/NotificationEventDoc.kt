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
import java.io.IOException

/**
 * Data class representing Notification event.
 */
data class NotificationEventDoc(
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
                    EVENT_TAG -> event = NotificationEvent.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing event doc")
                    }
                }
            }
            event ?: throw IllegalArgumentException("$EVENT_TAG field absent")
            return NotificationEventDoc(
                event
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(EVENT_TAG, event)
            .endObject()
    }
}
