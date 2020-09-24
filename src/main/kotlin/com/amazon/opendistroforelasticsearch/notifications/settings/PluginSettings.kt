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
import org.elasticsearch.cluster.service.ClusterService
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
    private const val EMAIL_CHANNEL_KEY = "opendistro.notifications.email.channel"

    /**
     * "From:" email address while sending email.
     */
    private const val EMAIL_FROM_ADDRESS_KEY = "opendistro.notifications.email.fromAddress"

    /**
     * Monthly email sending limit from this plugin.
     */
    private const val EMAIL_LIMIT_MONTHLY_KEY = "opendistro.notifications.email.monthlyLimit"

    /**
     * If the "From:" email address is set to below value then email will NOT be submitted to server.
     * any other valid "From:" email address would be submitted to server.
     */
    const val UNCONFIGURED_EMAIL_ADDRESS = "nobody@email.com" // Email will not be sent if email address different than this value

    @Volatile
    var emailChannel: String

    @Volatile
    var emailFromAddress: String

    @Volatile
    var emailMonthlyLimit: Int

    private const val DECIMAL_RADIX: Int = 10

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
                log.warn("$PLUGIN_NAME:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}")
            }
        }
        // Initialize the settings values to default values
        emailChannel = (settings?.get(EMAIL_CHANNEL_KEY) ?: EmailChannelType.SMTP.stringValue)
        emailFromAddress = (settings?.get(EMAIL_FROM_ADDRESS_KEY) ?: UNCONFIGURED_EMAIL_ADDRESS)
        emailMonthlyLimit = Integer.parseInt((settings?.get(EMAIL_LIMIT_MONTHLY_KEY) ?: "200"))

        defaultSettings = mapOf(
            EMAIL_CHANNEL_KEY to emailChannel,
            EMAIL_FROM_ADDRESS_KEY to emailFromAddress,
            EMAIL_LIMIT_MONTHLY_KEY to emailMonthlyLimit.toString(DECIMAL_RADIX)
        )
    }

    private val EMAIL_CHANNEL: Setting<String> = Setting.simpleString(
        EMAIL_CHANNEL_KEY,
        defaultSettings[EMAIL_CHANNEL_KEY],
        NodeScope, Dynamic
    )

    private val EMAIL_FROM_ADDRESS: Setting<String> = Setting.simpleString(
        EMAIL_FROM_ADDRESS_KEY,
        defaultSettings[EMAIL_FROM_ADDRESS_KEY],
        NodeScope, Dynamic
    )

    private val EMAIL_LIMIT_MONTHLY: Setting<Int> = Setting.intSetting(
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

    fun addSettingsUpdateConsumer(clusterService: ClusterService) {
        // Update the variables to setting values
        emailChannel = EMAIL_CHANNEL.get(clusterService.settings)
        emailFromAddress = EMAIL_FROM_ADDRESS.get(clusterService.settings)
        emailMonthlyLimit = EMAIL_LIMIT_MONTHLY.get(clusterService.settings)

        // Update the variables to cluster setting values
        // If the cluster is not yet started then we get default values again
        val clusterEmailChannel = clusterService.clusterSettings.get(EMAIL_CHANNEL)
        if (clusterEmailChannel != null) {
            log.info("$PLUGIN_NAME:$EMAIL_CHANNEL_KEY -autoUpdatedTo-> $clusterEmailChannel")
            emailChannel = clusterEmailChannel
        }
        val clusterEmailFromAddress = clusterService.clusterSettings.get(EMAIL_FROM_ADDRESS)
        if (clusterEmailFromAddress != null) {
            log.info("$PLUGIN_NAME:$EMAIL_FROM_ADDRESS_KEY -autoUpdatedTo-> $clusterEmailFromAddress")
            emailFromAddress = clusterEmailFromAddress
        }
        val clusterEmailMonthlyLimit = clusterService.clusterSettings.get(EMAIL_LIMIT_MONTHLY)
        if (clusterEmailMonthlyLimit != null) {
            log.info("$PLUGIN_NAME:$EMAIL_LIMIT_MONTHLY_KEY -autoUpdatedTo-> $clusterEmailMonthlyLimit")
            emailMonthlyLimit = clusterEmailMonthlyLimit
        }

        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_CHANNEL) {
            emailChannel = it
            log.info("$PLUGIN_NAME:$EMAIL_CHANNEL_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_FROM_ADDRESS) {
            emailFromAddress = it
            log.info("$PLUGIN_NAME:$EMAIL_FROM_ADDRESS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_LIMIT_MONTHLY) {
            emailMonthlyLimit = it
            log.info("$PLUGIN_NAME:$EMAIL_LIMIT_MONTHLY_KEY -updatedTo-> $it")
        }
    }
}
