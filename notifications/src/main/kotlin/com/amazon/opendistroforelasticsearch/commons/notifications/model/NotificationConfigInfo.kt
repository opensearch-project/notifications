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
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException
import java.time.Instant

/**
 * Data class representing Notification config.
 */
data class NotificationConfigInfo(
    val configId: String,
    val lastUpdatedTime: Instant,
    val createdTime: Instant,
    val tenant: String,
    val notificationConfig: NotificationConfig
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(configId)) { "id is null or empty" }
    }

    companion object {
        private val log by logger(NotificationConfigInfo::class.java)
        private const val CONFIG_ID_TAG = "configId"
        private const val UPDATED_TIME_TAG = "lastUpdatedTimeMs"
        private const val CREATED_TIME_TAG = "createdTimeMs"
        private const val TENANT_TAG = "tenant"
        private const val NOTIFICATION_CONFIG_TAG = "notificationConfig"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationConfigInfo(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationConfigInfo {
            var configId: String? = null
            var lastUpdatedTime: Instant? = null
            var createdTime: Instant? = null
            var tenant: String? = null
            var notificationConfig: NotificationConfig? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_ID_TAG -> configId = parser.text()
                    UPDATED_TIME_TAG -> lastUpdatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_TAG -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    TENANT_TAG -> tenant = parser.text()
                    NOTIFICATION_CONFIG_TAG -> notificationConfig = NotificationConfig.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing configuration")
                    }
                }
            }
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            lastUpdatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_TAG field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_TAG field absent")
            tenant = tenant ?: ""
            notificationConfig ?: throw IllegalArgumentException("$NOTIFICATION_CONFIG_TAG field absent")
            return NotificationConfigInfo(
                configId,
                lastUpdatedTime,
                createdTime,
                tenant,
                notificationConfig
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        configId = input.readString(),
        lastUpdatedTime = input.readInstant(),
        createdTime = input.readInstant(),
        tenant = input.readString(),
        notificationConfig = NotificationConfig.reader.read(input)
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(configId)
        output.writeInstant(lastUpdatedTime)
        output.writeInstant(createdTime)
        output.writeString(tenant)
        notificationConfig.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_TAG, configId)
            .field(UPDATED_TIME_TAG, lastUpdatedTime.toEpochMilli())
            .field(CREATED_TIME_TAG, createdTime.toEpochMilli())
            .field(TENANT_TAG, tenant)
            .field(NOTIFICATION_CONFIG_TAG, notificationConfig)
            .endObject()
    }
}
