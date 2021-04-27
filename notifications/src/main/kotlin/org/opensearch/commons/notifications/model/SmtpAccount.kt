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
import org.opensearch.commons.notifications.model.config.BaseConfigData
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
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(host)) { "host is null or empty" }
        require(port > 0)
        require(isValidEmail(fromAddress)) { "Invalid email address" }
    }

    enum class MethodType { None, Ssl, StartTls; }

    companion object {
        private val log by logger(NotificationConfig::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SmtpAccount(it) }

        const val HOST_FIELD = "host"
        const val PORT_FIELD = "port"
        const val METHOD_FIELD = "method"
        const val FROM_ADDRESS_FIELD = "fromAddress"
        const val USERNAME_FIELD = "username"
        const val PASSWORD_FIELD = "password"

        /**
         * @param parser XContentParser to deserialize data from.
         */
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

        /**
         * Creator used in REST communication.
         * @param configDataMap Map to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(configDataMap: Map<String, Any>): SmtpAccount {
            configDataMap[HOST_FIELD] ?: throw IllegalArgumentException("$HOST_FIELD field absent")
            configDataMap[PORT_FIELD] ?: throw IllegalArgumentException("$PORT_FIELD field absent")
            configDataMap[METHOD_FIELD] ?: throw IllegalArgumentException("$METHOD_FIELD field absent")
            configDataMap[FROM_ADDRESS_FIELD] ?: throw IllegalArgumentException("$FROM_ADDRESS_FIELD field absent")

            var username: SecureString? = null
            var password: SecureString? = null
            if (configDataMap.containsKey(USERNAME_FIELD)) {
                username = SecureString((configDataMap[USERNAME_FIELD] as String).toCharArray())
            }

            if (configDataMap.containsKey(PASSWORD_FIELD)) {
                password = SecureString((configDataMap[PASSWORD_FIELD] as String).toCharArray())
            }

            return SmtpAccount(
                configDataMap[HOST_FIELD] as String,
                // Covering both cases of port being String or Int
                (configDataMap[PORT_FIELD].toString()).toInt(),
                valueOf(configDataMap[METHOD_FIELD] as String, MethodType.None, log),
                configDataMap[FROM_ADDRESS_FIELD] as String,
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
