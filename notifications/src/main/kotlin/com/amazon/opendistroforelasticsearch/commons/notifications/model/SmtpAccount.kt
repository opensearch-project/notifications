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
package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.commons.utils.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.commons.utils.isValidEmail
import com.amazon.opendistroforelasticsearch.commons.utils.logger
import com.amazon.opendistroforelasticsearch.commons.utils.valueOf
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.settings.SecureString
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
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
