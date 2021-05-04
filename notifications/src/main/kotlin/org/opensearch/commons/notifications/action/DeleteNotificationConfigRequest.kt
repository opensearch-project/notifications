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
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_LIST_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class DeleteNotificationConfigRequest : ActionRequest, ToXContentObject {
    val configIds: Set<String>

    companion object {
        private val log by logger(DeleteNotificationConfigRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { DeleteNotificationConfigRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): DeleteNotificationConfigRequest {
            var configIds: Set<String>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    CONFIG_ID_LIST_TAG -> configIds = parser.stringList().toSet()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing DeleteNotificationConfigRequest")
                    }
                }
            }
            configIds ?: throw IllegalArgumentException("$CONFIG_ID_LIST_TAG field absent")
            return DeleteNotificationConfigRequest(configIds)
        }
    }

    /**
     * constructor for creating the class
     * @param configIds the id of the notification configuration
     */
    constructor(configIds: Set<String>) {
        this.configIds = configIds
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        configIds = input.readStringList().toSet()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeStringCollection(configIds)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(CONFIG_ID_LIST_TAG, configIds)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (configIds.isNullOrEmpty()) {
            validationException = ValidateActions.addValidationError("configIds is null or empty", validationException)
        }
        return validationException
    }
}
