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
package com.amazon.opensearch.commons.notifications.action

import com.amazon.opensearch.commons.notifications.model.NotificationConfig
import com.amazon.opensearch.commons.utils.logger
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Action request for creating new configuration.
 */
class UpdateNotificationConfigRequest : ActionRequest, ToXContentObject {
    val configId: String
    val notificationConfig: NotificationConfig

    companion object {
        private val log by logger(UpdateNotificationConfigRequest::class.java)
        private const val CONFIG_ID_TAG = "configId"
        private const val NOTIFICATION_CONFIG_TAG = "notificationConfig"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { UpdateNotificationConfigRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser, id: String? = null): UpdateNotificationConfigRequest {
            var configId: String? = id
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
                    NOTIFICATION_CONFIG_TAG -> notificationConfig = NotificationConfig.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing UpdateNotificationConfigRequest")
                    }
                }
            }
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            notificationConfig ?: throw IllegalArgumentException("$NOTIFICATION_CONFIG_TAG field absent")
            return UpdateNotificationConfigRequest(configId, notificationConfig)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_TAG, configId)
            .field(NOTIFICATION_CONFIG_TAG, notificationConfig)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param configId the id notification config object
     * @param notificationConfig the notification config object
     */
    constructor(configId: String, notificationConfig: NotificationConfig) {
        this.configId = configId
        this.notificationConfig = notificationConfig
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configId = input.readString()
        notificationConfig = NotificationConfig.reader.read(input)!!
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(configId)
        notificationConfig.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (Strings.isNullOrEmpty(configId)) {
            validationException = ValidateActions.addValidationError("configId is null or empty", validationException)
        }
        return validationException
    }
}
