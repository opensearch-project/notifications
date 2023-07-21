/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TAG
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.utils.logger
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG
import java.io.IOException

/**
 * Data class representing Notification config with metadata.
 */
data class NotificationConfigDoc(
    val metadata: DocMetadata,
    val config: NotificationConfig
) : ToXContent {

    companion object {
        private val log by logger(NotificationConfigDoc::class.java)

        /**
         * Parse the data from parser and create object
         * @param parser data referenced at parser
         * @return created object
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationConfigDoc {
            var metadata: DocMetadata? = null
            var config: NotificationConfig? = null

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
                    CONFIG_TAG -> config = NotificationConfig.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing configuration doc")
                    }
                }
            }
            metadata ?: throw IllegalArgumentException("$METADATA_TAG field absent")
            config ?: throw IllegalArgumentException("$CONFIG_TAG field absent")
            return NotificationConfigDoc(
                metadata,
                config
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
            .field(CONFIG_TAG, config)
            .endObject()
    }
}
