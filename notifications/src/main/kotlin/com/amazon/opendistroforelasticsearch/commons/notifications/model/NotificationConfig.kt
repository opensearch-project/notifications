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

import com.amazon.opendistroforelasticsearch.commons.utils.enumSet
import com.amazon.opendistroforelasticsearch.commons.utils.fieldIfNotNull
import com.amazon.opendistroforelasticsearch.commons.utils.logger
import com.amazon.opendistroforelasticsearch.commons.utils.valueOf
import org.elasticsearch.common.Strings
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
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
    val slack: Slack? = null,
    val chime: Chime? = null,
    val webhook: Webhook? = null,
    val email: Email? = null,
    val smtpAccount: SmtpAccount? = null,
    val emailGroup: EmailGroup? = null
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        when (configType) {
            ConfigType.Slack -> requireNotNull(slack)
            ConfigType.Chime -> requireNotNull(chime)
            ConfigType.Webhook -> requireNotNull(webhook)
            ConfigType.Email -> requireNotNull(email)
            ConfigType.SmtpAccount -> requireNotNull(smtpAccount)
            ConfigType.EmailGroup -> requireNotNull(emailGroup)
            ConfigType.None -> log.info("Some config field not recognized")
        }
    }

    companion object {
        private val log by logger(NotificationConfig::class.java)
        private const val NAME_TAG = "name"
        private const val DESCRIPTION_TAG = "description"
        private const val CONFIG_TYPE_TAG = "configType"
        private const val FEATURES_TAG = "features"
        private const val IS_ENABLED_TAG = "isEnabled"
        private const val SLACK_TAG = "slack"
        private const val CHIME_TAG = "chime"
        private const val WEBHOOK_TAG = "webhook"
        private const val EMAIL_TAG = "email"
        private const val SMTP_ACCOUNT_TAG = "smtpAccount"
        private const val EMAIL_GROUP_TAG = "emailGroup"

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
            var slack: Slack? = null
            var chime: Chime? = null
            var webhook: Webhook? = null
            var email: Email? = null
            var smtpAccount: SmtpAccount? = null
            var emailGroup: EmailGroup? = null

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
                    CONFIG_TYPE_TAG -> configType = valueOf(parser.text(), ConfigType.None, log)
                    FEATURES_TAG -> features = parser.enumSet(Feature.None, log)
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    SLACK_TAG -> slack = Slack.parse(parser)
                    CHIME_TAG -> chime = Chime.parse(parser)
                    WEBHOOK_TAG -> webhook = Webhook.parse(parser)
                    EMAIL_TAG -> email = Email.parse(parser)
                    SMTP_ACCOUNT_TAG -> smtpAccount = SmtpAccount.parse(parser)
                    EMAIL_GROUP_TAG -> emailGroup = EmailGroup.parse(parser)
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
                slack,
                chime,
                webhook,
                email,
                smtpAccount,
                emailGroup
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
        slack = input.readOptionalWriteable(Slack.reader),
        chime = input.readOptionalWriteable(Chime.reader),
        webhook = input.readOptionalWriteable(Webhook.reader),
        email = input.readOptionalWriteable(Email.reader),
        smtpAccount = input.readOptionalWriteable(SmtpAccount.reader),
        emailGroup = input.readOptionalWriteable(EmailGroup.reader)
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
        output.writeOptionalWriteable(slack)
        output.writeOptionalWriteable(chime)
        output.writeOptionalWriteable(webhook)
        output.writeOptionalWriteable(email)
        output.writeOptionalWriteable(smtpAccount)
        output.writeOptionalWriteable(emailGroup)
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
            .fieldIfNotNull(SLACK_TAG, slack)
            .fieldIfNotNull(CHIME_TAG, chime)
            .fieldIfNotNull(WEBHOOK_TAG, webhook)
            .fieldIfNotNull(EMAIL_TAG, email)
            .fieldIfNotNull(SMTP_ACCOUNT_TAG, smtpAccount)
            .fieldIfNotNull(EMAIL_GROUP_TAG, emailGroup)
            .endObject()
    }
}
