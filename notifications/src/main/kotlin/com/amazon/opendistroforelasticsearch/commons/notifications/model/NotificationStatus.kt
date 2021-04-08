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

import com.amazon.opendistroforelasticsearch.commons.utils.logger
import com.amazon.opendistroforelasticsearch.commons.utils.objectList
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
data class NotificationStatus(
    val notificationInfo: NotificationInfo,
    val statusList: List<ChannelStatus> = listOf()
) : BaseModel {

    companion object {
        private val log by logger(NotificationStatus::class.java)
        private const val NOTIFICATION_INFO_TAG = "notificationInfo"
        private const val STATUS_LIST_TAG = "statusList"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationStatus(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationStatus {
            var notificationInfo: NotificationInfo? = null
            var statusList: List<ChannelStatus> = listOf()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NOTIFICATION_INFO_TAG -> notificationInfo = NotificationInfo.parse(parser)
                    STATUS_LIST_TAG -> statusList = parser.objectList { ChannelStatus.parse(it) }
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing notification")
                    }
                }
            }
            notificationInfo ?: throw IllegalArgumentException("$NOTIFICATION_INFO_TAG field absent")

            return NotificationStatus(
                notificationInfo,
                statusList
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        notificationInfo = NotificationInfo.reader.read(input),
        statusList = input.readList(ChannelStatus.reader)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        notificationInfo.writeTo(output)
        output.writeList(statusList)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(NOTIFICATION_INFO_TAG, notificationInfo)
            .field(STATUS_LIST_TAG, statusList)
            .endObject()
    }
}
