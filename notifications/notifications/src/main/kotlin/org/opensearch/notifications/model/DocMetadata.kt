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

package org.opensearch.notifications.model

import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
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
        const val CREATED_TIME_TAG = "created_time_ms"
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
