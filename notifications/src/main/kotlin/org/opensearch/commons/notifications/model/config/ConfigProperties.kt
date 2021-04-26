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
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.notifications.model.ConfigType
import java.lang.UnsupportedOperationException


val SlackChannelProperties = object : ConfigDataProperties {

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
}


val ChimeChannelProperties = object : ConfigDataProperties {

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

}


val EmailChannelProperties = object : ConfigDataProperties {

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
}


val EmailGroupChannelProperties = object : ConfigDataProperties {

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
}


val SmtpAccountChannelProperties = object : ConfigDataProperties {

    /**
     * {@inheritDoc}
     */
    override fun getConfigDataReader(): Reader<SmtpAccount> {
        return Reader { SmtpAccount(it) }
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
}


val WebhookChannelProperties = object : ConfigDataProperties {

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
}


val NoOpProperties = object : ConfigDataProperties {

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
}

val CONFIG_PROPERTIES: List<ConfigDataProperties> = listOf(
        SlackChannelProperties,
        ChimeChannelProperties,
        WebhookChannelProperties,
        EmailChannelProperties,
        EmailGroupChannelProperties,
        SmtpAccountChannelProperties,
        NoOpProperties
)

val CONFIG_TYPE_VS_PROPERTIES: Map<ConfigType, ConfigDataProperties> = CONFIG_PROPERTIES
        .filter { c -> c.getConfigType() != ConfigType.None }
        .associate { c ->
            c.getConfigType() to c
        }

/**
 * Properties for ConfigTypes.
 * This interface is used to provide contract accross configTypes without reading into config data classes.
 */
interface ConfigDataProperties {

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
}

