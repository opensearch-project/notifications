/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.notifications.settings

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import org.apache.logging.log4j.LogManager
import org.elasticsearch.bootstrap.BootstrapInfo
import org.elasticsearch.common.settings.Setting
import org.elasticsearch.common.settings.Setting.Property.Dynamic
import org.elasticsearch.common.settings.Setting.Property.NodeScope
import org.elasticsearch.common.settings.Settings
import java.io.IOException
import java.nio.file.Path

/**
 * settings specific to Notifications Plugin.
 */
object PluginSettings {
    /**
     * Setting to choose smtp or SES for sending mail.
     */
    const val EMAIL_CHANNEL_KEY = "opendistro.notifications.email.channel"

    /**
     * "From:" email address while sending email.
     */
    const val EMAIL_FROM_ADDRESS_KEY = "opendistro.notifications.email.fromAddress"

    /**
     * Monthly email sending limit from this plugin.
     */
    const val EMAIL_LIMIT_MONTHLY_KEY = "opendistro.notifications.email.monthlyLimit"

    /**
     * If the "From:" email address is set to below value then email will NOT be submitted to server.
     * any other valid "From:" email address would be submitted to server.
     */
    const val UNCONFIGURED_EMAIL_ADDRESS = "nobody@email.com" // Email will not be sent if email address different than this value

    private val log = LogManager.getLogger(javaClass)
    private val defaultSettings: Map<String, String>

    init {
        var settings: Settings? = null
        val configDirName = BootstrapInfo.getSystemProperties()?.get("es.path.conf")?.toString()
        if (configDirName != null) {
            val defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "notifications.yml")
            try {
                settings = Settings.builder().loadFromPath(defaultSettingYmlFile).build()
            } catch (exception: IOException) {
                log.warn("Failed to load ${defaultSettingYmlFile.toAbsolutePath()}")
            }
        }
        defaultSettings = mapOf(
            EMAIL_CHANNEL_KEY to (settings?.get(EMAIL_CHANNEL_KEY) ?: EmailChannelType.SMTP.stringValue),
            EMAIL_FROM_ADDRESS_KEY to (settings?.get(EMAIL_FROM_ADDRESS_KEY) ?: UNCONFIGURED_EMAIL_ADDRESS),
            EMAIL_LIMIT_MONTHLY_KEY to (settings?.get(EMAIL_LIMIT_MONTHLY_KEY) ?: "200")
        )
    }

    val EMAIL_CHANNEL: Setting<String> = Setting.simpleString(
        EMAIL_CHANNEL_KEY,
        defaultSettings[EMAIL_CHANNEL_KEY],
        NodeScope, Dynamic
    )

    val EMAIL_FROM_ADDRESS: Setting<String> = Setting.simpleString(
        EMAIL_FROM_ADDRESS_KEY,
        defaultSettings[EMAIL_FROM_ADDRESS_KEY],
        NodeScope, Dynamic
    )

    val EMAIL_LIMIT_MONTHLY: Setting<Int> = Setting.intSetting(
        EMAIL_LIMIT_MONTHLY_KEY,
        Integer.parseInt(defaultSettings[EMAIL_LIMIT_MONTHLY_KEY]),
        NodeScope, Dynamic
    )

    /**
     * Returns list of additional settings available specific to this plugin.
     *
     * @return list of settings defined in this plugin
     */
    fun getAllSettings(): List<Setting<*>> {
        return listOf(EMAIL_CHANNEL,
            EMAIL_FROM_ADDRESS,
            EMAIL_LIMIT_MONTHLY
        )
    }
}
