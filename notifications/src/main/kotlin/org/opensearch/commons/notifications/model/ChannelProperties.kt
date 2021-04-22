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

import org.opensearch.common.io.stream.Writeable.Reader
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.CHIME_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.EMAIL_GROUP_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.EMAIL_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.SLACK_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.SMTP_ACCOUNT_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.WEBHOOK_TAG
import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass


val SlackChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return SLACK_TAG
    }

    override fun getChannelReader(): Reader<Slack> {
        return Slack.reader
    }

    override fun createChannelData(parser: XContentParser): Slack {
        return Slack.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.Slack
    }

    override fun getDataClass(): KClass<Slack> {
        return Slack::class
    }

}


val ChimeChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return CHIME_TAG
    }

    override fun getChannelReader(): Reader<Chime> {
        return Chime.reader
    }

    override fun createChannelData(parser: XContentParser): Chime {
        return Chime.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.Chime
    }

    override fun getDataClass(): KClass<Chime> {
        return Chime::class
    }
}


val EmailChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return EMAIL_TAG
    }

    override fun getChannelReader(): Reader<Email> {
        return Email.reader
    }

    override fun createChannelData(parser: XContentParser): Email {
        return Email.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.Email
    }

    override fun getDataClass(): KClass<Email> {
        return Email::class
    }
}


val EmailGroupChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return EMAIL_GROUP_TAG
    }

    override fun getChannelReader(): Reader<EmailGroup> {
        return EmailGroup.reader
    }

    override fun createChannelData(parser: XContentParser): EmailGroup {
        return EmailGroup.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.EmailGroup
    }

    override fun getDataClass(): KClass<EmailGroup> {
        return EmailGroup::class
    }
}


val SmtpAccountChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return SMTP_ACCOUNT_TAG
    }

    override fun getChannelReader(): Reader<SmtpAccount> {
        return SmtpAccount.reader
    }

    override fun createChannelData(parser: XContentParser): SmtpAccount {
        return SmtpAccount.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.SmtpAccount
    }

    override fun getDataClass(): KClass<SmtpAccount> {
        return SmtpAccount::class
    }
}


val WebhookChannelProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        return WEBHOOK_TAG
    }

    override fun getChannelReader(): Reader<Webhook> {
        return Webhook.reader
    }

    override fun createChannelData(parser: XContentParser): Webhook {
        return Webhook.parse(parser)
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.Webhook
    }

    override fun getDataClass(): KClass<Webhook> {
        return Webhook::class
    }
}


val NoOpProperties = object : ChannelProperties {
    override fun getChannelTag(): String {
        throw UnsupportedOperationException()
    }

    override fun getChannelReader(): Reader<Webhook> {
        throw UnsupportedOperationException()
    }

    override fun createChannelData(parser: XContentParser): Webhook {
        throw UnsupportedOperationException()
    }

    override fun getConfigType(): ConfigType {
        return ConfigType.None
    }

    override fun getDataClass(): KClass<out BaseChannelData> {
        throw UnsupportedOperationException()
    }
}

val CHANNEL_PROPERTIES: List<ChannelProperties> = listOf(
        SlackChannelProperties,
        ChimeChannelProperties,
        WebhookChannelProperties,
        EmailChannelProperties,
        EmailGroupChannelProperties,
        SmtpAccountChannelProperties,
        NoOpProperties
)

val DATA_CLASS_VS_CHANNEL_PROPERTIES: Map<KClass<out BaseChannelData>, ChannelProperties> = CHANNEL_PROPERTIES
        .filter { c -> c.getConfigType() != ConfigType.None }
        .associate { c ->
            c.getDataClass() to c
        }


interface ChannelProperties {
    fun getChannelTag(): String
    fun getChannelReader(): Reader<out BaseChannelData>
    fun createChannelData(parser: XContentParser): BaseChannelData
    fun getConfigType(): ConfigType
    fun getDataClass(): KClass<out BaseChannelData>
}

