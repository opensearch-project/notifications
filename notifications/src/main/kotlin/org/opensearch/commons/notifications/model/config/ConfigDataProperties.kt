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
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.model.BaseConfigData
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.notifications.model.XParser

internal object ConfigDataProperties {
    /**
     * Properties for ConfigTypes.
     * This data class is used to provide contract across configTypes without reading into config data classes.
     */
    private data class ConfigProperty(
        val configDataReader: Reader<out BaseConfigData>,
        val configDataParser: XParser<out BaseConfigData>
    )

    private val CONFIG_PROPERTIES_MAP = mapOf(
        Pair(ConfigType.SLACK, ConfigProperty(Slack.reader, Slack.xParser)),
        Pair(ConfigType.CHIME, ConfigProperty(Chime.reader, Chime.xParser)),
        Pair(ConfigType.WEBHOOK, ConfigProperty(Webhook.reader, Webhook.xParser)),
        Pair(ConfigType.EMAIL, ConfigProperty(Email.reader, Email.xParser)),
        Pair(ConfigType.EMAIL_GROUP, ConfigProperty(EmailGroup.reader, EmailGroup.xParser)),
        Pair(ConfigType.SMTP_ACCOUNT, ConfigProperty(SmtpAccount.reader, SmtpAccount.xParser))
    )

    /**
     * Get Reader for provided config type
     * @param @ConfigType
     * @return Reader
     */
    fun getReaderForConfigType(configType: ConfigType): Reader<out BaseConfigData> {
        return CONFIG_PROPERTIES_MAP[configType]?.configDataReader
            ?: throw IllegalArgumentException("Transport action used with unknown ConfigType:$configType")
    }

    /**
     * Validate config data is of ConfigType
     */
    fun validateConfigData(configType: ConfigType, configData: BaseConfigData?): Boolean {
        return when (configType) {
            ConfigType.SLACK -> configData is Slack
            ConfigType.WEBHOOK -> configData is Webhook
            ConfigType.EMAIL -> configData is Email
            ConfigType.EMAIL_GROUP -> configData is EmailGroup
            ConfigType.SMTP_ACCOUNT -> configData is SmtpAccount
            ConfigType.CHIME -> configData is Chime
            ConfigType.NONE -> true
        }
    }

    /**
     * Creates config data from parser for given configType
     * @param configType the ConfigType
     * @param parser parser for ConfigType
     * @return created BaseConfigData on success. null if configType is not recognized
     *
     */
    fun createConfigData(configType: ConfigType, parser: XContentParser): BaseConfigData? {
        return CONFIG_PROPERTIES_MAP[configType]?.configDataParser?.parse(parser)
    }
}
