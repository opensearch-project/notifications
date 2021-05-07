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

package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_NAME_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_RECIPIENT_STATUS_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_DETAIL_TAG
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.objectList
import java.io.IOException

/**
 * Data class representing Notification Status.
 */
data class ChannelStatus(
    val configId: String,
    val configName: String,
    val configType: ConfigType,
    val emailRecipientStatus: List<EmailRecipientStatus> = listOf(),
    val deliveryStatus: DeliveryStatus? = null
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(configId)) { "config id is null or empty" }
        require(!Strings.isNullOrEmpty(configName)) { "config name is null or empty" }
        when (configType) {
            ConfigType.CHIME -> requireNotNull(deliveryStatus)
            ConfigType.WEBHOOK -> requireNotNull(deliveryStatus)
            ConfigType.SLACK -> requireNotNull(deliveryStatus)
            ConfigType.EMAIL -> require(emailRecipientStatus.isNotEmpty())
            ConfigType.NONE -> log.info("Some config field not recognized")
            else -> {
                log.info("non-allowed config type for Status")
            }
        }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { ChannelStatus(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): ChannelStatus {
            var configName: String? = null
            var configId: String? = null
            var configType: ConfigType? = null
            var emailRecipientStatus: List<EmailRecipientStatus> = listOf()
            var deliveryStatus: DeliveryStatus? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_NAME_TAG -> configName = parser.text()
                    CONFIG_ID_TAG -> configId = parser.text()
                    CONFIG_TYPE_TAG -> configType = ConfigType.fromTagOrDefault(parser.text())
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

            return ChannelStatus(
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
        configType = input.readEnum(ConfigType::class.java),
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
            .field(CONFIG_TYPE_TAG, configType.tag)
            .field(CONFIG_NAME_TAG, configName)
            .field(EMAIL_RECIPIENT_STATUS_TAG, emailRecipientStatus)
            .fieldIfNotNull(STATUS_DETAIL_TAG, deliveryStatus)
            .endObject()
    }
}
