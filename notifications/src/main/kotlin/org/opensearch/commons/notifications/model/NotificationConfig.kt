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
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.DESCRIPTION_TAG
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.IS_ENABLED_TAG
import org.opensearch.commons.notifications.NotificationConstants.NAME_TAG
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.createConfigData
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.getReaderForConfigType
import org.opensearch.commons.notifications.model.config.ConfigDataProperties.validateConfigData
import org.opensearch.commons.utils.enumSet
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import java.io.IOException
import java.util.EnumSet

/**
 * Data class representing Notification config.
 */
data class NotificationConfig(
    val name: String,
    val description: String,
    val configType: ConfigType,
    val features: EnumSet<Feature>,
    val configData: BaseConfigData?,
    val isEnabled: Boolean = true
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        if (!validateConfigData(configType, configData)) {
            throw IllegalArgumentException("ConfigType: $configType and data doesn't match")
        }
        if (configType === ConfigType.NONE) {
            log.info("Some config field not recognized")
        }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)

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
            var configData: BaseConfigData? = null
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    DESCRIPTION_TAG -> description = parser.text()
                    CONFIG_TYPE_TAG -> configType = ConfigType.fromTagOrDefault(parser.text())
                    FEATURE_LIST_TAG -> features = parser.enumSet(Feature.enumParser)
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    else -> {
                        val configTypeForTag = ConfigType.fromTagOrDefault(fieldName)
                        if (configTypeForTag != ConfigType.NONE && configData == null) {
                            configData = createConfigData(configTypeForTag, parser)
                        } else {
                            parser.skipChildren()
                            log.info("Unexpected field: $fieldName, while parsing configuration")
                        }
                    }
                }
            }
            name ?: throw IllegalArgumentException("$NAME_TAG field absent")
            configType ?: throw IllegalArgumentException("$CONFIG_TYPE_TAG field absent")
            features ?: throw IllegalArgumentException("$FEATURE_LIST_TAG field absent")
            return NotificationConfig(
                name,
                description,
                configType,
                features,
                configData,
                isEnabled
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(NAME_TAG, name)
            .field(DESCRIPTION_TAG, description)
            .field(CONFIG_TYPE_TAG, configType.tag)
            .field(FEATURE_LIST_TAG, features)
            .field(IS_ENABLED_TAG, isEnabled)
            .fieldIfNotNull(configType.tag, configData)
            .endObject()
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
        configData = input.readOptionalWriteable(getReaderForConfigType(input.readEnum(ConfigType::class.java)))
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
        // Reading config types multiple times in constructor
        output.writeEnum(configType)
        output.writeOptionalWriteable(configData)
    }
}
