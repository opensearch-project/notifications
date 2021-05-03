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
import org.opensearch.common.settings.SecureString
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.FROM_ADDRESS_FIELD
import org.opensearch.commons.notifications.NotificationConstants.HOST_FIELD
import org.opensearch.commons.notifications.NotificationConstants.METHOD_FIELD
import org.opensearch.commons.notifications.NotificationConstants.PASSWORD_FIELD
import org.opensearch.commons.notifications.NotificationConstants.PORT_FIELD
import org.opensearch.commons.notifications.NotificationConstants.USERNAME_FIELD
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.isValidEmail
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.valueOf
import java.io.IOException

/**
 * Data class representing SMTP account channel.
 */
data class SmtpAccount(
    val host: String,
    val port: Int,
    val method: MethodType,
    val fromAddress: String,
    val username: SecureString? = null,
    val password: SecureString? = null
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(host)) { "host is null or empty" }
        require(port > 0)
        require(isValidEmail(fromAddress)) { "Invalid email address" }
    }

    enum class MethodType { None, Ssl, StartTls; }

    companion object {
        private val log by logger(SmtpAccount::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SmtpAccount(it) }

        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SmtpAccount {
            var host: String? = null
            var port: Int? = null
            var method: MethodType? = null
            var fromAddress: String? = null
            var username: SecureString? = null
            var password: SecureString? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    HOST_FIELD -> host = parser.text()
                    PORT_FIELD -> port = parser.intValue()
                    METHOD_FIELD -> method = valueOf(parser.text(), MethodType.None, log)
                    FROM_ADDRESS_FIELD -> fromAddress = parser.text()
                    USERNAME_FIELD -> username = SecureString(parser.text().toCharArray())
                    PASSWORD_FIELD -> password = SecureString(parser.text().toCharArray())
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing SmtpAccount")
                    }
                }
            }
            host ?: throw IllegalArgumentException("$HOST_FIELD field absent")
            port ?: throw IllegalArgumentException("$PORT_FIELD field absent")
            method ?: throw IllegalArgumentException("$METHOD_FIELD field absent")
            fromAddress ?: throw IllegalArgumentException("$FROM_ADDRESS_FIELD field absent")
            return SmtpAccount(
                host,
                port,
                method,
                fromAddress,
                username,
                password
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
        builder.field(HOST_FIELD, host)
            .field(PORT_FIELD, port)
            .field(METHOD_FIELD, method)
            .field(FROM_ADDRESS_FIELD, fromAddress)
            .fieldIfNotNull(USERNAME_FIELD, username?.toString())
            .fieldIfNotNull(PASSWORD_FIELD, password?.toString())
        return builder.endObject()
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        host = input.readString(),
        port = input.readInt(),
        method = input.readEnum(MethodType::class.java),
        fromAddress = input.readString(),
        username = input.readOptionalSecureString(),
        password = input.readOptionalSecureString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(out: StreamOutput) {
        out.writeString(host)
        out.writeInt(port)
        out.writeEnum(method)
        out.writeString(fromAddress)
        out.writeOptionalSecureString(username)
        out.writeOptionalSecureString(password)
    }
}
