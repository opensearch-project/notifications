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

import com.amazon.opendistroforelasticsearch.notifications.util.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import com.amazon.opendistroforelasticsearch.notifications.util.objectList
import com.amazon.opendistroforelasticsearch.notifications.util.valueOf
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing Notification Status.
 */
data class NotificationStatus(
    val configId: String,
    val configName: String,
    val configType: NotificationConfig.ConfigType,
    val emailRecipientStatus: List<EmailRecipientStatus> = listOf(),
    val deliveryStatus: DeliveryStatus? = null
) : Writeable, ToXContent {

    init {
        require(!Strings.isNullOrEmpty(configId)) { "config id is null or empty" }
        require(!Strings.isNullOrEmpty(configName)) { "config name is null or empty" }
        when (configType) {
            NotificationConfig.ConfigType.Chime -> requireNotNull(deliveryStatus)
            NotificationConfig.ConfigType.Webhook -> requireNotNull(deliveryStatus)
            NotificationConfig.ConfigType.Slack -> requireNotNull(deliveryStatus)
            NotificationConfig.ConfigType.Email -> require(emailRecipientStatus.isEmpty())
            NotificationConfig.ConfigType.None -> log.info("Some config field not recognized")
            else -> {
                log.info("non-allowed config type for Status")
            }
        }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)
        private const val CONFIG_ID_TAG = "configId"
        private const val CONFIG_NAME_TAG = "configName"
        private const val CONFIG_TYPE_TAG = "configType"
        private const val EMAIL_RECIPIENT_STATUS_TAG = "emailRecipientStatus"
        private const val STATUS_DETAIL_TAG = "deliveryStatus"

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
            var configName: String? = null
            var configId: String? = null
            var configType: NotificationConfig.ConfigType? = null
            var emailRecipientStatus: List<EmailRecipientStatus> = listOf()
            var deliveryStatus: DeliveryStatus? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser::getTokenLocation
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_NAME_TAG -> configName = parser.text()
                    CONFIG_ID_TAG -> configId = parser.text()
                    CONFIG_TYPE_TAG -> configType = valueOf(parser.text(), NotificationConfig.ConfigType.None)
                    EMAIL_RECIPIENT_STATUS_TAG -> emailRecipientStatus = parser.objectList { EmailRecipientStatus.parse(it) }
                    STATUS_DETAIL_TAG -> deliveryStatus = DeliveryStatus.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing Notification Status")
                    }
                }
            }
            configName ?: throw IllegalArgumentException("$CONFIG_NAME_TAG field absent")
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            configType ?: throw IllegalArgumentException("$CONFIG_TYPE_TAG field absent")

            return NotificationStatus(
                configId,
                configName,
                configType,
                emailRecipientStatus,
                deliveryStatus
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        configId = input.readString(),
        configName = input.readString(),
        configType = input.readEnum(NotificationConfig.ConfigType::class.java),
        emailRecipientStatus = input.readList(EmailRecipientStatus.reader),
        deliveryStatus = input.readOptionalWriteable(DeliveryStatus.reader)
        )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(configId)
        output.writeString(configName)
        output.writeEnum(configType)
        output.writeCollection(emailRecipientStatus)
        output.writeOptionalWriteable(deliveryStatus)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_TAG, configId)
            .field(CONFIG_TYPE_TAG, configType)
            .field(CONFIG_NAME_TAG, configName)
            .field(EMAIL_RECIPIENT_STATUS_TAG, emailRecipientStatus)
            .fieldIfNotNull(STATUS_DETAIL_TAG, deliveryStatus)
            .endObject()
    }
}
