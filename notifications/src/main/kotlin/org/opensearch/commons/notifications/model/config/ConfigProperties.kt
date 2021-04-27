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

package org.opensearch.commons.notifications.model.config

import org.opensearch.common.io.stream.Writeable.Reader
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.CHIME_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.EMAIL_GROUP_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.EMAIL_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.SLACK_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.SMTP_ACCOUNT_TAG
import org.opensearch.commons.notifications.model.NotificationConfig.Companion.WEBHOOK_TAG
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.notifications.model.ConfigType
import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

val SlackChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return SLACK_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<Slack> {
        return Slack.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): Slack {
        return Slack.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.Slack
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<Slack> {
        return Slack::class
    }
}

val ChimeChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return CHIME_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<Chime> {
        return Chime.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): Chime {
        return Chime.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.Chime
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<Chime> {
        return Chime::class
    }
}

val EmailChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return EMAIL_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<Email> {
        return Email.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): Email {
        return Email.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.Email
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<Email> {
        return Email::class
    }
}

val EmailGroupChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return EMAIL_GROUP_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<EmailGroup> {
        return EmailGroup.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): EmailGroup {
        return EmailGroup.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.EmailGroup
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<EmailGroup> {
        return EmailGroup::class
    }
}

val SmtpAccountChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return SMTP_ACCOUNT_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<SmtpAccount> {
        return SmtpAccount.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): SmtpAccount {
        return SmtpAccount.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.SmtpAccount
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<SmtpAccount> {
        return SmtpAccount::class
    }
}

val WebhookChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        return WEBHOOK_TAG
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<Webhook> {
        return Webhook.reader
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): Webhook {
        return Webhook.parse(configDataMap)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.Webhook
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<Webhook> {
        return Webhook::class
    }
}

val NoOpProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getChannelTag(): String {
        throw UnsupportedOperationException()
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<Webhook> {
        throw UnsupportedOperationException()
    }

    /**
     * {@inheritDoc}
     */
    override fun createConfigData(configDataMap: Map<String, Any>): Webhook {
        throw UnsupportedOperationException()
    }

    /**
     * {@inheritDoc}
     */
    override fun getConfigType(): ConfigType {
        return ConfigType.None
    }

    /**
     * {@inheritDoc}
     */
    override fun getDataClass(): KClass<out BaseConfigData> {
        throw UnsupportedOperationException()
    }
}

/**
 * Properties for ConfigTypes.
 * This interface is used to provide contract accross configTypes without reading into config data classes.
 */
interface ConfigDataProperties {

    /**
     * @return ChannelTag for concrete ConfigType
     */
    fun getChannelTag(): String

    /**
     * @return Reader for concrete ConfigType.
     */
    fun getConfigDataReader(): Reader<out BaseConfigData>

    /**
     * Create ConfigData for provided parser, by calling data class' parse.
     * @return Created ConfigData
     */
    fun createConfigData(configDataMap: Map<String, Any>): BaseConfigData

    /**
     * @return ConfigType for concrete implementation
     */
    fun getConfigType(): ConfigType

    /**
     * @return Klass for concrete implementation, to be used to map with config data class.
     */
    fun getDataClass(): KClass<out BaseConfigData>
}
