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
import org.opensearch.commons.utils.enumSet
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.valueOf
import java.io.IOException
import java.util.ArrayList
import java.util.EnumSet

/**
 * Data class representing Notification config.
 */
data class NotificationConfig(
        val name: String,
        val description: String,
        val configType: ConfigType,
        val features: EnumSet<Feature>,
        val isEnabled: Boolean = true,
        val channelDataList: List<BaseChannelData?> = listOf(),
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        if (configType === ConfigType.None) {
            log.info("Some config field not recognized")
        }
        channelDataList.forEach { c -> requireNotNull(c) }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)
        const val NAME_TAG = "name"
        const val DESCRIPTION_TAG = "description"
        const val CONFIG_TYPE_TAG = "configType"
        const val FEATURES_TAG = "features"
        const val IS_ENABLED_TAG = "isEnabled"
        const val SLACK_TAG = "slack"
        const val CHIME_TAG = "chime"
        const val WEBHOOK_TAG = "webhook"
        const val EMAIL_TAG = "email"
        const val SMTP_ACCOUNT_TAG = "smtpAccount"
        const val EMAIL_GROUP_TAG = "emailGroup"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { NotificationConfig(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @Suppress("ComplexMethod")
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): NotificationConfig {
            var name: String? = null
            var description = ""
            var configType: ConfigType? = null
            var features: EnumSet<Feature>? = null
            var isEnabled = true
            val channelDataList = ArrayList<BaseChannelData>()

            XContentParserUtils.ensureExpectedToken(
                    XContentParser.Token.START_OBJECT,
                    parser.currentToken(),
                    parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()

                if (isValidChannelTag(fieldName)) {
                    val channelData = createChannelData(fieldName, parser)
                    if (channelData != null) {
                        channelDataList.add(channelData)
                    }
                    continue
                }

                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    DESCRIPTION_TAG -> description = parser.text()
                    CONFIG_TYPE_TAG -> configType = valueOf(parser.text(), ConfigType.None, log)
                    FEATURES_TAG -> features = parser.enumSet(Feature.None, log)
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing configuration")
                    }
                }
            }
            name ?: throw IllegalArgumentException("$NAME_TAG field absent")
            configType ?: throw IllegalArgumentException("$CONFIG_TYPE_TAG field absent")
            features ?: throw IllegalArgumentException("$FEATURES_TAG field absent")
            return NotificationConfig(
                    name,
                    description,
                    configType,
                    features,
                    isEnabled,
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
            name = input.readString(),
            description = input.readString(),
            configType = input.readEnum(ConfigType::class.java),
            features = input.readEnumSet(Feature::class.java),
            isEnabled = input.readBoolean(),
            channelDataList = CHANNEL_PROPERTIES.filter { prop -> ConfigType.None != prop.getConfigType() }
                    .map { prop ->
                        input.readOptionalWriteable(prop.getChannelReader())
                    }
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(name)
        output.writeString(description)
        output.writeEnum(configType)
        output.writeEnumSet(features)
        output.writeBoolean(isEnabled)
        for (channelData in channelDataList) {
            output.writeOptionalWriteable(channelData)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
                .field(NAME_TAG, name)
                .field(DESCRIPTION_TAG, description)
                .field(CONFIG_TYPE_TAG, configType)
                .field(FEATURES_TAG, features)
                .field(IS_ENABLED_TAG, isEnabled)

        for (channelData in channelDataList) {
            val channelTag = DATA_CLASS_VS_CHANNEL_PROPERTIES.get(channelData!!::class)?.getChannelTag()
            if (channelTag != null) {
                builder.field(channelTag, channelData)
            }
        }

        builder.endObject()

        return builder
    }
}
