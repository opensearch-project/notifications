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
package org.opensearch.commons.notifications.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.DELETE_RESPONSE_LIST_TAG
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.enumReader
import org.opensearch.commons.utils.enumWriter
import org.opensearch.commons.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class DeleteNotificationConfigResponse : BaseResponse {
    val configIdToStatus: Map<String, RestStatus>

    companion object {
        private val log by logger(DeleteNotificationConfigResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { DeleteNotificationConfigResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): DeleteNotificationConfigResponse {
            var configIdToStatus: Map<String, RestStatus>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    DELETE_RESPONSE_LIST_TAG -> configIdToStatus = convertMapStrings(parser.mapStrings())
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing DeleteNotificationConfigResponse")
                    }
                }
            }
            configIdToStatus ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            return DeleteNotificationConfigResponse(configIdToStatus)
        }

        private fun convertMapStrings(inputMap: Map<String, String>): Map<String, RestStatus> {
            return inputMap.mapValues { RestStatus.valueOf(it.value) }
        }
    }

    /**
     * constructor for creating the class
     * @param configIdToStatus the ids of the deleted notification configuration with status
     */
    constructor(configIdToStatus: Map<String, RestStatus>) {
        this.configIdToStatus = configIdToStatus
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configIdToStatus = input.readMap(STRING_READER, enumReader(RestStatus::class.java))
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeMap(configIdToStatus, STRING_WRITER, enumWriter(RestStatus::class.java))
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(DELETE_RESPONSE_LIST_TAG, configIdToStatus)
            .endObject()
    }

    override fun getStatus(): RestStatus {
        val distinctStatus = configIdToStatus.values.distinct()
        return when {
            distinctStatus.size > 1 -> RestStatus.MULTI_STATUS
            distinctStatus.size == 1 -> distinctStatus[0]
            else -> RestStatus.NOT_MODIFIED
        }
    }
}
