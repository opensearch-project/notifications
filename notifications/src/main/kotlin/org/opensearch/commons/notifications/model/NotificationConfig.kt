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
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.config.BaseConfigData
import org.opensearch.commons.notifications.model.config.CONFIG_PROPERTIES
import org.opensearch.commons.notifications.model.config.createConfigData
import org.opensearch.commons.utils.enumSet
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.valueOf
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
        val isEnabled: Boolean = true,
        val configData: BaseConfigData
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        if (configType === ConfigType.None) {
            log.info("Some config field not recognized")
        }
        requireNotNull(configData)
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)
        const val NAME_TAG = "name"
        const val DESCRIPTION_TAG = "description"
        const val CONFIG_TYPE_TAG = "configType"
        const val FEATURES_TAG = "features"
        const val IS_ENABLED_TAG = "isEnabled"
        const val CONFIG_DATA_TAG = "configData"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader<NotificationConfig> { input ->
            val name = input!!.readString()
            val description = input.readString()
            val configType = input.readEnum(ConfigType::class.java)
            val features = input.readEnumSet(Feature::class.java)
            val isEnabled = input.readBoolean()
            val configData = input.readOptionalWriteable(CONFIG_PROPERTIES.first { c ->
                c.getConfigType() == configType
            }.getConfigDataReader())

            NotificationConfig(name, description, configType, features, isEnabled, configData!!)
        }

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
            val configData: BaseConfigData?


            XContentParserUtils.ensureExpectedToken(
                    XContentParser.Token.START_OBJECT,
                    parser.currentToken(),
                    parser
            )
            var tempConfigDataMap: Map<String, Any>? = null
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    DESCRIPTION_TAG -> description = parser.text()
                    CONFIG_TYPE_TAG -> configType = valueOf(parser.text(), ConfigType.None, log)
                    FEATURES_TAG -> features = parser.enumSet(Feature.None, log)
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    CONFIG_DATA_TAG -> tempConfigDataMap = parser.map()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing configuration")
                    }
                }
            }
            name ?: throw IllegalArgumentException("$NAME_TAG field absent")
            configType ?: throw IllegalArgumentException("$CONFIG_TYPE_TAG field absent")
            features ?: throw IllegalArgumentException("$FEATURES_TAG field absent")
            tempConfigDataMap ?: throw IllegalArgumentException("$CONFIG_DATA_TAG field absent")

            configData = createConfigData(configType, tempConfigDataMap)!!

            return NotificationConfig(
                    name,
                    description,
                    configType,
                    features,
                    isEnabled,
                    configData
            )
        }
    }


    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(name)
        output.writeString(description)
        output.writeEnum(configType)
        output.writeEnumSet(features)
        output.writeBoolean(isEnabled)
        output.writeOptionalWriteable(configData)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {

        builder!!
        return builder.startObject()
                .field(NAME_TAG, name)
                .field(DESCRIPTION_TAG, description)
                .field(CONFIG_TYPE_TAG, configType)
                .field(FEATURES_TAG, features)
                .field(IS_ENABLED_TAG, isEnabled)
                .field(CONFIG_DATA_TAG, configData)
                .endObject()
    }
}
