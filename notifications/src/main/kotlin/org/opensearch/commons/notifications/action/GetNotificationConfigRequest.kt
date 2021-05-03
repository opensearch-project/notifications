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

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.DEFAULT_MAX_ITEMS
import org.opensearch.commons.notifications.NotificationConstants.FROM_INDEX_TAG
import org.opensearch.commons.notifications.NotificationConstants.MAX_ITEMS_TAG
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class GetNotificationConfigRequest : ActionRequest, ToXContentObject {
    val fromIndex: Int
    val maxItems: Int
    val configId: String?

    companion object {
        private val log by logger(GetNotificationConfigRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetNotificationConfigRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetNotificationConfigRequest {
            var fromIndex = 0
            var maxItems = DEFAULT_MAX_ITEMS
            var configId: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    FROM_INDEX_TAG -> fromIndex = parser.intValue()
                    MAX_ITEMS_TAG -> maxItems = parser.intValue()
                    CONFIG_ID_TAG -> configId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing GetNotificationConfigRequest")
                    }
                }
            }
            return GetNotificationConfigRequest(fromIndex, maxItems, configId)
        }
    }

    /**
     * constructor for creating the class
     * @param fromIndex the starting index for paginated response
     * @param maxItems the maximum number of items to return for paginated response
     * @param configId the id of the notification configuration
     */
    constructor(fromIndex: Int = 0, maxItems: Int = DEFAULT_MAX_ITEMS, configId: String? = null) {
        this.fromIndex = fromIndex
        this.maxItems = maxItems
        this.configId = configId
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        fromIndex = input.readInt()
        maxItems = input.readInt()
        configId = input.readOptionalString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeInt(fromIndex)
        output.writeInt(maxItems)
        output.writeOptionalString(configId)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(FROM_INDEX_TAG, fromIndex)
            .field(MAX_ITEMS_TAG, maxItems)
            .fieldIfNotNull(CONFIG_ID_TAG, configId)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (fromIndex < 0) {
            validationException = ValidateActions.addValidationError("fromIndex is -ve", validationException)
        }
        if (maxItems <= 0) {
            validationException = ValidateActions.addValidationError("maxItems is not +ve", validationException)
        }
        return validationException
    }
}
