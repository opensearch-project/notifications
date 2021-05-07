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

package org.opensearch.notifications.settings

import org.opensearch.bootstrap.BootstrapInfo
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Setting.Property.Dynamic
import org.opensearch.common.settings.Setting.Property.NodeScope
import org.opensearch.common.settings.Settings
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import software.amazon.awssdk.regions.Region
import java.io.IOException
import java.nio.file.Path

/**
 * settings specific to Notifications Plugin.
 */
internal object PluginSettings {

    /**
     * Settings Key prefix for this plugin.
     */
    private const val KEY_PREFIX = "opensearch.notifications"

    /**
     * General settings Key prefix.
     */
    private const val GENERAL_KEY_PREFIX = "$KEY_PREFIX.general"

    /**
     * Settings Key prefix for this plugin.
     */
    private const val EMAIL_KEY_PREFIX = "$KEY_PREFIX.email"

    /**
     * Access settings Key prefix.
     */
    private const val ACCESS_KEY_PREFIX = "$KEY_PREFIX.access"

    /**
     * Operation timeout for network operations.
     */
    private const val OPERATION_TIMEOUT_MS_KEY = "$GENERAL_KEY_PREFIX.operationTimeoutMs"

    /**
     * Setting to choose default number of items to query.
     */
    private const val DEFAULT_ITEMS_QUERY_COUNT_KEY = "$GENERAL_KEY_PREFIX.defaultItemsQueryCount"

    /**
     * Setting to choose smtp or SES for sending mail.
     */
    private const val EMAIL_CHANNEL_KEY = "$EMAIL_KEY_PREFIX.channel"

    /**
     * "From:" email address while sending email.
     */
    private const val EMAIL_FROM_ADDRESS_KEY = "$EMAIL_KEY_PREFIX.fromAddress"

    /**
     * Monthly email sending limit from this plugin.
     */
    private const val EMAIL_LIMIT_MONTHLY_KEY = "$EMAIL_KEY_PREFIX.monthlyLimit"

    /**
     * Email size limit.
     */
    private const val EMAIL_SIZE_LIMIT_KEY = "$EMAIL_KEY_PREFIX.sizeLimit"

    /**
     * Amazon SES AWS region to send mail to.
     */
    private const val EMAIL_SES_AWS_REGION_KEY = "$EMAIL_KEY_PREFIX.ses.awsRegion"

    /**
     * SMTP host address to send mail to.
     */
    private const val EMAIL_SMTP_HOST_KEY = "$EMAIL_KEY_PREFIX.smtp.host"

    /**
     * SMTP port number to send mail to.
     */
    private const val EMAIL_SMTP_PORT_KEY = "$EMAIL_KEY_PREFIX.smtp.port"

    /**
     * SMTP Transport method. starttls, ssl or plain.
     */
    private const val EMAIL_SMTP_TRANSPORT_METHOD_KEY = "$EMAIL_KEY_PREFIX.smtp.transportMethod"

    /**
     * Setting to choose admin access restriction.
     */
    private const val ADMIN_ACCESS_KEY = "$ACCESS_KEY_PREFIX.adminAccess"

    /**
     * Setting to choose filter method.
     */
    private const val FILTER_BY_KEY = "$ACCESS_KEY_PREFIX.filterBy"

    /**
     * Setting to choose ignored roles for filtering.
     */
    private const val IGNORE_ROLE_KEY = "$ACCESS_KEY_PREFIX.ignoreRoles"

    /**
     * Default operation timeout for network operations.
     */
    private const val DEFAULT_OPERATION_TIMEOUT_MS = 60000L

    /**
     * Minimum operation timeout for network operations.
     */
    private const val MINIMUM_OPERATION_TIMEOUT_MS = 100L

    /**
     * Default number of items to query.
     */
    private const val DEFAULT_ITEMS_QUERY_COUNT_VALUE = 100

    /**
     * Minimum number of items to query.
     */
    private const val MINIMUM_ITEMS_QUERY_COUNT = 10

    /**
     * Default email channel.
     */
    private val DEFAULT_EMAIL_CHANNEL = EmailChannelType.SMTP.stringValue

    /**
     * Default monthly email sending limit from this plugin.
     */
    private const val DEFAULT_EMAIL_LIMIT_MONTHLY = 200

    /**
     * Default email size limit as 10MB.
     */
    private const val DEFAULT_EMAIL_SIZE_LIMIT = 10000000

    /**
     * Minimum email size limit as 10KB.
     */
    private const val MINIMUM_EMAIL_SIZE_LIMIT = 10000

    /**
     * Default Amazon SES AWS region.
     */
    private val DEFAULT_SES_AWS_REGION = Region.US_WEST_2.id()

    /**
     * Default SMTP Host name to connect to.
     */
    private const val DEFAULT_SMTP_HOST = "localhost"

    /**
     * Default SMTP port number to connect to.
     */
    private const val DEFAULT_SMTP_PORT = 587

    /**
     * Default SMTP transport method.
     */
    private const val DEFAULT_SMTP_TRANSPORT_METHOD = "starttls"

    /**
     * If the "From:" email address is set to below value then email will NOT be submitted to server.
     * any other valid "From:" email address would be submitted to server.
     */
    const val UNCONFIGURED_EMAIL_ADDRESS =
        "nobody@email.com" // Email will not be sent if email address different than this value

    /**
     * Default admin access method.
     */
    private const val DEFAULT_ADMIN_ACCESS_METHOD = "All"

    /**
     * Default filter-by method.
     */
    private const val DEFAULT_FILTER_BY_METHOD = "NoFilter"

    /**
     * Default filter-by method.
     */
    private val DEFAULT_IGNORED_ROLES = listOf(
        "own_index",
        "kibana_user",
        "notifications_full_access",
        "notifications_read_access"
    )

    /**
     * Operation timeout setting in ms for I/O operations
     */
    @Volatile
    var operationTimeoutMs: Long

    /**
     * Default number of items to query.
     */
    @Volatile
    var defaultItemsQueryCount: Int

    /**
     * Email channel setting [EmailChannelType] in string format
     */
    @Volatile
    var emailChannel: String

    /**
     * Email "From:" Address setting
     */
    @Volatile
    var emailFromAddress: String

    /**
     * Email monthly throttle limit setting
     */
    @Volatile
    var emailMonthlyLimit: Int

    /**
     * Email size limit setting
     */
    @Volatile
    var emailSizeLimit: Int

    /**
     * Amazon SES AWS region setting
     */
    @Volatile
    var sesAwsRegion: String

    /**
     * SMTP server host setting
     */
    @Volatile
    var smtpHost: String

    /**
     * SMTP server port setting
     */
    @Volatile
    var smtpPort: Int

    /**
     * SMTP server transport method setting
     */
    @Volatile
    var smtpTransportMethod: String

    /**
     * admin access method.
     */
    @Volatile
    var adminAccess: AdminAccess

    /**
     * Filter-by method.
     */
    @Volatile
    var filterBy: FilterBy

    /**
     * list of ignored roles.
     */
    @Volatile
    var ignoredRoles: List<String>

    /**
     * Enum for types of admin access
     * "Standard" -> Admin user access follows standard user
     * "All" -> Admin user with "all_access" role can see all data of all users.
     */
    internal enum class AdminAccess { Standard, All }

    /**
     * Enum for types of filterBy options
     * NoFilter -> everyone see each other's configurations
     * User -> configurations are visible to only themselves
     * Roles -> configurations are visible to users having any one of the role of creator
     * BackendRoles -> configurations are visible to users having any one of the backend role of creator
     */
    internal enum class FilterBy { NoFilter, User, Roles, BackendRoles }

    private const val DECIMAL_RADIX: Int = 10

    private val log by logger(javaClass)
    private val defaultSettings: Map<String, String>

    init {
        var settings: Settings? = null
        val configDirName = BootstrapInfo.getSystemProperties()?.get("opensearch.path.conf")?.toString()
        if (configDirName != null) {
            val defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "notifications.yml")
            try {
                settings = Settings.builder().loadFromPath(defaultSettingYmlFile).build()
            } catch (exception: IOException) {
                log.warn("$LOG_PREFIX:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}")
            }
        }
        // Initialize the settings values to default values
        operationTimeoutMs = (settings?.get(OPERATION_TIMEOUT_MS_KEY)?.toLong()) ?: DEFAULT_OPERATION_TIMEOUT_MS
        defaultItemsQueryCount = (settings?.get(DEFAULT_ITEMS_QUERY_COUNT_KEY)?.toInt())
            ?: DEFAULT_ITEMS_QUERY_COUNT_VALUE
        emailChannel = (settings?.get(EMAIL_CHANNEL_KEY) ?: DEFAULT_EMAIL_CHANNEL)
        emailFromAddress = (settings?.get(EMAIL_FROM_ADDRESS_KEY) ?: UNCONFIGURED_EMAIL_ADDRESS)
        emailMonthlyLimit = (settings?.get(EMAIL_LIMIT_MONTHLY_KEY)?.toInt()) ?: DEFAULT_EMAIL_LIMIT_MONTHLY
        emailSizeLimit = (settings?.get(EMAIL_SIZE_LIMIT_KEY)?.toInt()) ?: DEFAULT_EMAIL_SIZE_LIMIT
        sesAwsRegion = (settings?.get(EMAIL_SES_AWS_REGION_KEY) ?: DEFAULT_SES_AWS_REGION)
        smtpHost = (settings?.get(EMAIL_SMTP_HOST_KEY) ?: DEFAULT_SMTP_HOST)
        smtpPort = (settings?.get(EMAIL_SMTP_PORT_KEY)?.toInt()) ?: DEFAULT_SMTP_PORT
        smtpTransportMethod = (settings?.get(EMAIL_SMTP_TRANSPORT_METHOD_KEY) ?: DEFAULT_SMTP_TRANSPORT_METHOD)
        adminAccess = AdminAccess.valueOf(settings?.get(ADMIN_ACCESS_KEY) ?: DEFAULT_ADMIN_ACCESS_METHOD)
        filterBy = FilterBy.valueOf(settings?.get(FILTER_BY_KEY) ?: DEFAULT_FILTER_BY_METHOD)
        ignoredRoles = settings?.getAsList(IGNORE_ROLE_KEY) ?: DEFAULT_IGNORED_ROLES

        defaultSettings = mapOf(
            OPERATION_TIMEOUT_MS_KEY to operationTimeoutMs.toString(DECIMAL_RADIX),
            DEFAULT_ITEMS_QUERY_COUNT_KEY to defaultItemsQueryCount.toString(DECIMAL_RADIX),
            EMAIL_CHANNEL_KEY to emailChannel,
            EMAIL_FROM_ADDRESS_KEY to emailFromAddress,
            EMAIL_LIMIT_MONTHLY_KEY to emailMonthlyLimit.toString(DECIMAL_RADIX),
            EMAIL_SIZE_LIMIT_KEY to emailSizeLimit.toString(DECIMAL_RADIX),
            EMAIL_SES_AWS_REGION_KEY to sesAwsRegion,
            EMAIL_SMTP_HOST_KEY to smtpHost,
            EMAIL_SMTP_PORT_KEY to smtpPort.toString(DECIMAL_RADIX),
            EMAIL_SMTP_TRANSPORT_METHOD_KEY to smtpTransportMethod,
            ADMIN_ACCESS_KEY to adminAccess.name,
            FILTER_BY_KEY to filterBy.name
        )
    }

    private val OPERATION_TIMEOUT_MS: Setting<Long> = Setting.longSetting(
        OPERATION_TIMEOUT_MS_KEY,
        defaultSettings[OPERATION_TIMEOUT_MS_KEY]!!.toLong(),
        MINIMUM_OPERATION_TIMEOUT_MS,
        NodeScope, Dynamic
    )

    private val DEFAULT_ITEMS_QUERY_COUNT: Setting<Int> = Setting.intSetting(
        DEFAULT_ITEMS_QUERY_COUNT_KEY,
        defaultSettings[DEFAULT_ITEMS_QUERY_COUNT_KEY]!!.toInt(),
        MINIMUM_ITEMS_QUERY_COUNT,
        NodeScope, Dynamic
    )

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
        defaultSettings[EMAIL_LIMIT_MONTHLY_KEY]!!.toInt(),
        0,
        NodeScope, Dynamic
    )

    private val EMAIL_SIZE_LIMIT: Setting<Int> = Setting.intSetting(
        EMAIL_SIZE_LIMIT_KEY,
        defaultSettings[EMAIL_SIZE_LIMIT_KEY]!!.toInt(),
        MINIMUM_EMAIL_SIZE_LIMIT,
        NodeScope, Dynamic
    )

    private val EMAIL_SES_AWS_REGION: Setting<String> = Setting.simpleString(
        EMAIL_SES_AWS_REGION_KEY,
        defaultSettings[EMAIL_SES_AWS_REGION_KEY],
        NodeScope, Dynamic
    )

    private val EMAIL_SMTP_HOST: Setting<String> = Setting.simpleString(
        EMAIL_SMTP_HOST_KEY,
        defaultSettings[EMAIL_SMTP_HOST_KEY],
        NodeScope, Dynamic
    )

    private val EMAIL_SMTP_PORT: Setting<Int> = Setting.intSetting(
        EMAIL_SMTP_PORT_KEY,
        defaultSettings[EMAIL_SMTP_PORT_KEY]!!.toInt(),
        0,
        NodeScope, Dynamic
    )

    private val EMAIL_SMTP_TRANSPORT_METHOD: Setting<String> = Setting.simpleString(
        EMAIL_SMTP_TRANSPORT_METHOD_KEY,
        defaultSettings[EMAIL_SMTP_TRANSPORT_METHOD_KEY],
        NodeScope, Dynamic
    )

    private val ADMIN_ACCESS: Setting<String> = Setting.simpleString(
        ADMIN_ACCESS_KEY,
        defaultSettings[ADMIN_ACCESS_KEY]!!,
        NodeScope, Dynamic
    )

    private val FILTER_BY: Setting<String> = Setting.simpleString(
        FILTER_BY_KEY,
        defaultSettings[FILTER_BY_KEY]!!,
        NodeScope, Dynamic
    )

    private val IGNORED_ROLES: Setting<List<String>> = Setting.listSetting(
        IGNORE_ROLE_KEY,
        DEFAULT_IGNORED_ROLES,
        { it },
        NodeScope, Dynamic
    )

    /**
     * Returns list of additional settings available specific to this plugin.
     *
     * @return list of settings defined in this plugin
     */
    fun getAllSettings(): List<Setting<*>> {
        return listOf(
            OPERATION_TIMEOUT_MS,
            DEFAULT_ITEMS_QUERY_COUNT,
            EMAIL_CHANNEL,
            EMAIL_FROM_ADDRESS,
            EMAIL_LIMIT_MONTHLY,
            EMAIL_SIZE_LIMIT,
            EMAIL_SES_AWS_REGION,
            EMAIL_SMTP_HOST,
            EMAIL_SMTP_PORT,
            EMAIL_SMTP_TRANSPORT_METHOD,
            ADMIN_ACCESS,
            FILTER_BY,
            IGNORED_ROLES
        )
    }

    /**
     * Update the setting variables to setting values from local settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromLocal(clusterService: ClusterService) {
        operationTimeoutMs = OPERATION_TIMEOUT_MS.get(clusterService.settings)
        defaultItemsQueryCount = DEFAULT_ITEMS_QUERY_COUNT.get(clusterService.settings)
        emailChannel = EMAIL_CHANNEL.get(clusterService.settings)
        emailFromAddress = EMAIL_FROM_ADDRESS.get(clusterService.settings)
        emailMonthlyLimit = EMAIL_LIMIT_MONTHLY.get(clusterService.settings)
        emailSizeLimit = EMAIL_SIZE_LIMIT.get(clusterService.settings)
        sesAwsRegion = EMAIL_SES_AWS_REGION.get(clusterService.settings)
        smtpHost = EMAIL_SMTP_HOST.get(clusterService.settings)
        smtpPort = EMAIL_SMTP_PORT.get(clusterService.settings)
        smtpTransportMethod = EMAIL_SMTP_TRANSPORT_METHOD.get(clusterService.settings)
        adminAccess = AdminAccess.valueOf(ADMIN_ACCESS.get(clusterService.settings))
        filterBy = FilterBy.valueOf(FILTER_BY.get(clusterService.settings))
        ignoredRoles = IGNORED_ROLES.get(clusterService.settings)
    }

    /**
     * Update the setting variables to setting values from cluster settings
     * @param clusterService cluster service instance
     */
    @Suppress("LongMethod")
    private fun updateSettingValuesFromCluster(clusterService: ClusterService) {
        val clusterOperationTimeoutMs = clusterService.clusterSettings.get(OPERATION_TIMEOUT_MS)
        if (clusterOperationTimeoutMs != null) {
            log.debug("$LOG_PREFIX:$OPERATION_TIMEOUT_MS_KEY -autoUpdatedTo-> $clusterOperationTimeoutMs")
            operationTimeoutMs = clusterOperationTimeoutMs
        }
        val clusterDefaultItemsQueryCount = clusterService.clusterSettings.get(DEFAULT_ITEMS_QUERY_COUNT)
        if (clusterDefaultItemsQueryCount != null) {
            log.debug("$LOG_PREFIX:$DEFAULT_ITEMS_QUERY_COUNT_KEY -autoUpdatedTo-> $clusterDefaultItemsQueryCount")
            defaultItemsQueryCount = clusterDefaultItemsQueryCount
        }
        val clusterEmailChannel = clusterService.clusterSettings.get(EMAIL_CHANNEL)
        if (clusterEmailChannel != null) {
            log.debug("$LOG_PREFIX:$EMAIL_CHANNEL_KEY -autoUpdatedTo-> $clusterEmailChannel")
            emailChannel = clusterEmailChannel
        }
        val clusterEmailFromAddress = clusterService.clusterSettings.get(EMAIL_FROM_ADDRESS)
        if (clusterEmailFromAddress != null) {
            log.debug("$LOG_PREFIX:$EMAIL_FROM_ADDRESS_KEY -autoUpdatedTo-> $clusterEmailFromAddress")
            emailFromAddress = clusterEmailFromAddress
        }
        val clusterEmailMonthlyLimit = clusterService.clusterSettings.get(EMAIL_LIMIT_MONTHLY)
        if (clusterEmailMonthlyLimit != null) {
            log.debug("$LOG_PREFIX:$EMAIL_LIMIT_MONTHLY_KEY -autoUpdatedTo-> $clusterEmailMonthlyLimit")
            emailMonthlyLimit = clusterEmailMonthlyLimit
        }
        val clusterEmailSizeLimit = clusterService.clusterSettings.get(EMAIL_SIZE_LIMIT)
        if (clusterEmailSizeLimit != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SIZE_LIMIT_KEY -autoUpdatedTo-> $clusterEmailSizeLimit")
            emailSizeLimit = clusterEmailSizeLimit
        }
        val clusterSesAwsRegion = clusterService.clusterSettings.get(EMAIL_SES_AWS_REGION)
        if (clusterSesAwsRegion != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SES_AWS_REGION_KEY -autoUpdatedTo-> $clusterSesAwsRegion")
            sesAwsRegion = clusterSesAwsRegion
        }
        val clusterSmtpHost = clusterService.clusterSettings.get(EMAIL_SMTP_HOST)
        if (clusterSmtpHost != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SMTP_HOST_KEY -autoUpdatedTo-> $clusterSmtpHost")
            smtpHost = clusterSmtpHost
        }
        val clusterSmtpPort = clusterService.clusterSettings.get(EMAIL_SMTP_PORT)
        if (clusterSmtpPort != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SMTP_PORT_KEY -autoUpdatedTo-> $clusterSmtpPort")
            smtpPort = clusterSmtpPort
        }
        val clusterSmtpTransportMethod = clusterService.clusterSettings.get(EMAIL_SMTP_TRANSPORT_METHOD)
        if (clusterSmtpTransportMethod != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SMTP_TRANSPORT_METHOD_KEY -autoUpdatedTo-> $clusterSmtpTransportMethod")
            smtpTransportMethod = clusterSmtpTransportMethod
        }
        val clusterAdminAccess = clusterService.clusterSettings.get(ADMIN_ACCESS)
        if (clusterAdminAccess != null) {
            log.debug("$LOG_PREFIX:$ADMIN_ACCESS_KEY -autoUpdatedTo-> $clusterAdminAccess")
            adminAccess = AdminAccess.valueOf(clusterAdminAccess)
        }
        val clusterFilterBy = clusterService.clusterSettings.get(FILTER_BY)
        if (clusterFilterBy != null) {
            log.debug("$LOG_PREFIX:$FILTER_BY_KEY -autoUpdatedTo-> $clusterFilterBy")
            filterBy = FilterBy.valueOf(clusterFilterBy)
        }
        val clusterIgnoredRoles = clusterService.clusterSettings.get(IGNORED_ROLES)
        if (clusterIgnoredRoles != null) {
            log.debug("$LOG_PREFIX:$IGNORE_ROLE_KEY -autoUpdatedTo-> $clusterIgnoredRoles")
            ignoredRoles = clusterIgnoredRoles
        }
    }

    /**
     * adds Settings update listeners to all settings.
     * @param clusterService cluster service instance
     */
    fun addSettingsUpdateConsumer(clusterService: ClusterService) {
        updateSettingValuesFromLocal(clusterService)
        // Update the variables to cluster setting values
        // If the cluster is not yet started then we get default values again
        updateSettingValuesFromCluster(clusterService)

        clusterService.clusterSettings.addSettingsUpdateConsumer(OPERATION_TIMEOUT_MS) {
            operationTimeoutMs = it
            log.info("$LOG_PREFIX:$OPERATION_TIMEOUT_MS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(DEFAULT_ITEMS_QUERY_COUNT) {
            defaultItemsQueryCount = it
            log.info("$LOG_PREFIX:$DEFAULT_ITEMS_QUERY_COUNT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_CHANNEL) {
            emailChannel = it
            log.info("$LOG_PREFIX:$EMAIL_CHANNEL_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_FROM_ADDRESS) {
            emailFromAddress = it
            log.info("$LOG_PREFIX:$EMAIL_FROM_ADDRESS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_LIMIT_MONTHLY) {
            emailMonthlyLimit = it
            log.info("$LOG_PREFIX:$EMAIL_LIMIT_MONTHLY_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SIZE_LIMIT) {
            emailSizeLimit = it
            log.info("$LOG_PREFIX:$EMAIL_SIZE_LIMIT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SES_AWS_REGION) {
            sesAwsRegion = it
            log.info("$LOG_PREFIX:$EMAIL_SES_AWS_REGION_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SMTP_HOST) {
            smtpHost = it
            log.info("$LOG_PREFIX:$EMAIL_SMTP_HOST_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SMTP_PORT) {
            smtpPort = it
            log.info("$LOG_PREFIX:$EMAIL_SMTP_PORT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SMTP_TRANSPORT_METHOD) {
            smtpTransportMethod = it
            log.info("$LOG_PREFIX:$EMAIL_SMTP_TRANSPORT_METHOD_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(ADMIN_ACCESS) {
            adminAccess = AdminAccess.valueOf(it)
            log.info("$LOG_PREFIX:$ADMIN_ACCESS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(FILTER_BY) {
            filterBy = FilterBy.valueOf(it)
            log.info("$LOG_PREFIX:$FILTER_BY_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(IGNORED_ROLES) {
            ignoredRoles = it
            log.info("$LOG_PREFIX:$IGNORE_ROLE_KEY -updatedTo-> $it")
        }
    }
}
