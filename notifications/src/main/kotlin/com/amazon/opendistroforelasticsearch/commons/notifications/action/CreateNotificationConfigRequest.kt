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
package com.amazon.opendistroforelasticsearch.commons.notifications.action

import com.amazon.opendistroforelasticsearch.commons.notifications.model.NotificationConfig
import com.amazon.opendistroforelasticsearch.commons.utils.logger
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Action request for creating new configuration.
 */
class CreateNotificationConfigRequest : ActionRequest, ToXContentObject {
    val notificationConfig: NotificationConfig

    companion object {
        private val log by logger(CreateNotificationConfigResponse::class.java)
        private const val NOTIFICATION_CONFIG_TAG = "notificationConfig"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateNotificationConfigRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): CreateNotificationConfigRequest {
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
                    NOTIFICATION_CONFIG_TAG -> notificationConfig = NotificationConfig.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing CreateNotificationConfigRequest")
                    }
                }
            }
            notificationConfig ?: throw IllegalArgumentException("$NOTIFICATION_CONFIG_TAG field absent")
            return CreateNotificationConfigRequest(notificationConfig)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(NOTIFICATION_CONFIG_TAG, notificationConfig)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param notificationConfig the notification config object
     */
    constructor(notificationConfig: NotificationConfig) {
        this.notificationConfig = notificationConfig
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        notificationConfig = NotificationConfig.reader.read(input)!!
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        notificationConfig.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
