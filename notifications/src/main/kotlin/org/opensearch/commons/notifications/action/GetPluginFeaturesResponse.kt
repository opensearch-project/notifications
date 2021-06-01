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
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.PLUGIN_FEATURES_TAG
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import java.io.IOException

/**
 * Action Response for getting notification plugin features.
 */
class GetPluginFeaturesResponse : BaseResponse {
    val configTypeList: List<String>
    val pluginFeatures: Map<String, String>

    companion object {
        private val log by logger(GetPluginFeaturesResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetPluginFeaturesResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetPluginFeaturesResponse {
            var configTypeList: List<String>? = null
            var pluginFeatures: Map<String, String>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_TYPE_LIST_TAG -> configTypeList = parser.stringList()
                    PLUGIN_FEATURES_TAG -> pluginFeatures = parser.mapStrings()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing DeleteNotificationConfigResponse")
                    }
                }
            }
            configTypeList ?: throw IllegalArgumentException("$CONFIG_TYPE_LIST_TAG field absent")
            pluginFeatures ?: throw IllegalArgumentException("$PLUGIN_FEATURES_TAG field absent")
            return GetPluginFeaturesResponse(configTypeList, pluginFeatures)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(CONFIG_TYPE_LIST_TAG, configTypeList)
            .field(PLUGIN_FEATURES_TAG, pluginFeatures)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param configTypeList the list of config types supported by plugin
     * @param pluginFeatures the map of plugin features supported to its value
     */
    constructor(configTypeList: List<String>, pluginFeatures: Map<String, String>) {
        this.configTypeList = configTypeList
        this.pluginFeatures = pluginFeatures
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configTypeList = input.readStringList()
        pluginFeatures = input.readMap(STRING_READER, STRING_READER)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeStringCollection(configTypeList)
        output.writeMap(pluginFeatures, STRING_WRITER, STRING_WRITER)
    }
}
