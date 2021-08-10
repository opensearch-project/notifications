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

package org.opensearch.notifications.model

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
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_TAG
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.utils.logger
import java.io.IOException

/**
 * Action Request to send test notification.
 */
class SendTestNotificationRequest : ActionRequest, ToXContentObject {
    val feature: Feature
    val configId: String

    companion object {
        private val log by logger(SendTestNotificationRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SendTestNotificationRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SendTestNotificationRequest {
            var feature: Feature? = null
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
                    FEATURE_TAG -> feature = Feature.fromTagOrDefault(parser.text())
                    CONFIG_ID_TAG -> configId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SendTestNotificationRequest")
                    }
                }
            }
            feature ?: throw IllegalArgumentException("$FEATURE_TAG field absent")
            configId ?: throw IllegalArgumentException("$CONFIG_ID_TAG field absent")
            return SendTestNotificationRequest(feature, configId)
        }
    }

    /**
     * constructor for creating the class
     * @param feature the notification info
     * @param configId the id of the notification configuration channel
     */
    constructor(
        feature: Feature,
        configId: String,
    ) {
        this.feature = feature
        this.configId = configId
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        feature = Feature.fromTagOrDefault(input.readString())
        configId = input.readString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(feature.tag)
        output.writeString(configId)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(FEATURE_TAG, feature)
            .field(CONFIG_ID_TAG, configId)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (configId.isEmpty()) {
            validationException = ValidateActions.addValidationError("config id is empty", validationException)
        }
        return validationException
    }
}
