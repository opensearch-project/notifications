/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.setting

import org.opensearch.bootstrap.BootstrapInfo
import org.opensearch.cluster.ClusterName.CLUSTER_NAME_SETTING
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.SecureSetting
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Setting.Property.Deprecated
import org.opensearch.common.settings.Setting.Property.Dynamic
import org.opensearch.common.settings.Setting.Property.Final
import org.opensearch.common.settings.Setting.Property.NodeScope
import org.opensearch.common.settings.Settings
import org.opensearch.core.common.settings.SecureString
import org.opensearch.notifications.core.NotificationCorePlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.core.NotificationCorePlugin.Companion.PLUGIN_NAME
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.SecureDestinationSettings
import java.io.IOException
import java.nio.file.Path

internal object PluginSettings {
    /**
     * Settings Key prefix for this plugin.
     */
    private const val KEY_PREFIX = "opensearch.notifications.core"

    /**
     * Settings Key prefix for Email.
     */
    private const val EMAIL_KEY_PREFIX = "$KEY_PREFIX.email"

    /**
     * Legacy Email Destination setting prefix used by Alerting.
     * Defining this here to be used as a fallback for the Notification plugin setting to account for migrated Email Destinations.
     */
    private const val LEGACY_EMAIL_DESTINATION_SETTING_PREFIX = "plugins.alerting.destination.email."

    /**
     * Settings Key prefix for Email. Note: Should contain . at the end for secure settings
     */
    private const val EMAIL_DESTINATION_SETTING_PREFIX = "$KEY_PREFIX.email."

    /**
     * Settings Key prefix for http connection.
     */
    private const val HTTP_CONNECTION_KEY_PREFIX = "$KEY_PREFIX.http"

    /**
     * Email size limit.
     */
    private const val EMAIL_SIZE_LIMIT_KEY = "$EMAIL_KEY_PREFIX.size_limit"

    /**
     * Email minimum header length.
     */
    private const val EMAIL_MINIMUM_HEADER_LENGTH_KEY = "$EMAIL_KEY_PREFIX.minimum_header_length"

    /**
     * Settings Key prefix for http connection.
     */
    private const val MAX_CONNECTIONS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.max_connections"

    /**
     * Settings Key prefix for max http connection per route.
     */
    private const val MAX_CONNECTIONS_PER_ROUTE_KEY = "$HTTP_CONNECTION_KEY_PREFIX.max_connection_per_route"

    /**
     * Settings Key prefix for connection timeout in milliseconds
     */
    private const val CONNECTION_TIMEOUT_MILLISECONDS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.connection_timeout"

    /**
     * Settings Key prefix for socket timeout in milliseconds
     */
    private const val SOCKET_TIMEOUT_MILLISECONDS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.socket_timeout"

    /**
     * Legacy setting for list of host deny list in Alerting
     */
    private const val LEGACY_ALERTING_HOST_DENY_LIST_KEY = "opendistro.destination.host.deny_list"

    /**
     * Setting for list of host deny list in Alerting
     */
    private const val ALERTING_HOST_DENY_LIST_KEY = "plugins.destination.host.deny_list"

    /**
     * Setting for list of host deny list
     */
    private const val HOST_DENY_LIST_KEY = "$HTTP_CONNECTION_KEY_PREFIX.host_deny_list"

    /**
     * Setting to choose allowed config types.
     */
    private const val ALLOWED_CONFIG_TYPE_KEY = "$KEY_PREFIX.allowed_config_types"

    /**
     * Setting to enable tooltip in UI
     */
    private const val TOOLTIP_SUPPORT_KEY = "$KEY_PREFIX.tooltip_support"

    /**
     * Setting to provide cluster name, which is <AWS-account-number:AWS-domain-name> on the managed service
     */
    private const val CLUSTER_NAME = "cluster.name"

    /**
     * Default cluster name if it cannot be retrieved.
     */
    private const val DEFAULT_CLUSTER_NAME = "OpenSearch:DefaultClusterName"

    /**
     * Default email size limit as 10MB.
     */
    private const val DEFAULT_EMAIL_SIZE_LIMIT = 10000000

    /**
     * Minimum email size limit as 10KB.
     */
    private const val MINIMUM_EMAIL_SIZE_LIMIT = 10000

    /**
     * Default value  for http connection.
     */
    private const val DEFAULT_MAX_CONNECTIONS = 60

    /**
     * Default value for max http connection per route.
     */
    private const val DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20

    /**
     * Default value for connection timeout in milliseconds
     */
    private const val DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 5000

    /**
     * Default value for socket timeout in milliseconds
     */
    private const val DEFAULT_SOCKET_TIMEOUT_MILLISECONDS = 50000

    /**
     * Default email header length. minimum value from 100 reference emails
     */
    private const val DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH = 160

    /**
     * Default config type list
     */
    private val DEFAULT_ALLOWED_CONFIG_TYPES = listOf(
        "slack",
        "chime",
        "microsoft_teams",
        "webhook",
        "email",
        "sns",
        "ses_account",
        "smtp_account",
        "email_group"
    )

    /**
     * Default host deny list
     */
    private val DEFAULT_HOST_DENY_LIST = emptyList<String>()

    /**
     * Default disable tooltip support
     */
    private const val DEFAULT_TOOLTIP_SUPPORT = true

    /**
     * Default destination settings
     */
    private val DEFAULT_DESTINATION_SETTINGS = emptyMap<String, SecureDestinationSettings>()

    /**
     * list of allowed config types.
     */
    @Volatile
    var allowedConfigTypes: List<String>

    /**
     * Email size limit setting
     */
    @Volatile
    var emailSizeLimit: Int

    /**
     * Email minimum header length setting
     */
    @Volatile
    var emailMinimumHeaderLength: Int

    /**
     * Http max connections
     */
    @Volatile
    var maxConnections: Int

    /**
     * Http max connections per route
     */
    @Volatile
    var maxConnectionsPerRoute: Int

    /**
     * Connection timeout
     */
    @Volatile
    var connectionTimeout: Int

    /**
     * Socket timeout
     */
    @Volatile
    var socketTimeout: Int

    /**
     * Tooltip support
     */
    @Volatile
    var tooltipSupport: Boolean

    /**
     * list of allowed config types.
     */
    @Volatile
    var hostDenyList: List<String>

    /**
     * cluster name
     */
    @Volatile
    var clusterName: String

    /**
     * Destination Settings
     */
    @Volatile
    var destinationSettings: Map<String, SecureDestinationSettings>

    private const val DECIMAL_RADIX: Int = 10

    private val log by logger(javaClass)
    private val defaultSettings: Map<String, String>

    init {
        var settings: Settings? = null
        val configDirName = BootstrapInfo.getSystemProperties()?.get("opensearch.path.conf")?.toString()
        if (configDirName != null) {
            val defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "notifications-core.yml")
            try {
                settings = Settings.builder().loadFromPath(defaultSettingYmlFile).build()
            } catch (e: IOException) {
                log.warn("$LOG_PREFIX:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}:${e.message}")
            }
        }
        // Initialize the settings values to default values
        emailSizeLimit = (settings?.get(EMAIL_SIZE_LIMIT_KEY)?.toInt()) ?: DEFAULT_EMAIL_SIZE_LIMIT
        emailMinimumHeaderLength = (settings?.get(EMAIL_MINIMUM_HEADER_LENGTH_KEY)?.toInt())
            ?: DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH
        maxConnections = (settings?.get(MAX_CONNECTIONS_KEY)?.toInt()) ?: DEFAULT_MAX_CONNECTIONS
        maxConnectionsPerRoute = (settings?.get(MAX_CONNECTIONS_PER_ROUTE_KEY)?.toInt())
            ?: DEFAULT_MAX_CONNECTIONS_PER_ROUTE
        connectionTimeout = (settings?.get(CONNECTION_TIMEOUT_MILLISECONDS_KEY)?.toInt())
            ?: DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS
        socketTimeout = (settings?.get(SOCKET_TIMEOUT_MILLISECONDS_KEY)?.toInt()) ?: DEFAULT_SOCKET_TIMEOUT_MILLISECONDS
        allowedConfigTypes = settings?.getAsList(ALLOWED_CONFIG_TYPE_KEY, null) ?: DEFAULT_ALLOWED_CONFIG_TYPES
        tooltipSupport = settings?.getAsBoolean(TOOLTIP_SUPPORT_KEY, true) ?: DEFAULT_TOOLTIP_SUPPORT
        hostDenyList = settings?.getAsList(HOST_DENY_LIST_KEY, null) ?: DEFAULT_HOST_DENY_LIST
        clusterName = settings?.get(CLUSTER_NAME, DEFAULT_CLUSTER_NAME) ?: DEFAULT_CLUSTER_NAME
        destinationSettings = if (settings != null) loadDestinationSettings(settings) else DEFAULT_DESTINATION_SETTINGS

        defaultSettings = mapOf(
            EMAIL_SIZE_LIMIT_KEY to emailSizeLimit.toString(DECIMAL_RADIX),
            EMAIL_MINIMUM_HEADER_LENGTH_KEY to emailMinimumHeaderLength.toString(DECIMAL_RADIX),
            MAX_CONNECTIONS_KEY to maxConnections.toString(DECIMAL_RADIX),
            MAX_CONNECTIONS_PER_ROUTE_KEY to maxConnectionsPerRoute.toString(DECIMAL_RADIX),
            CONNECTION_TIMEOUT_MILLISECONDS_KEY to connectionTimeout.toString(DECIMAL_RADIX),
            SOCKET_TIMEOUT_MILLISECONDS_KEY to socketTimeout.toString(DECIMAL_RADIX),
            TOOLTIP_SUPPORT_KEY to tooltipSupport.toString()
        )
    }

    val EMAIL_SIZE_LIMIT: Setting<Int> = Setting.intSetting(
        EMAIL_SIZE_LIMIT_KEY,
        defaultSettings[EMAIL_SIZE_LIMIT_KEY]!!.toInt(),
        MINIMUM_EMAIL_SIZE_LIMIT,
        NodeScope,
        Dynamic
    )

    val EMAIL_MINIMUM_HEADER_LENGTH: Setting<Int> = Setting.intSetting(
        EMAIL_MINIMUM_HEADER_LENGTH_KEY,
        defaultSettings[EMAIL_MINIMUM_HEADER_LENGTH_KEY]!!.toInt(),
        NodeScope,
        Dynamic
    )

    val MAX_CONNECTIONS: Setting<Int> = Setting.intSetting(
        MAX_CONNECTIONS_KEY,
        defaultSettings[MAX_CONNECTIONS_KEY]!!.toInt(),
        NodeScope,
        Dynamic
    )

    val MAX_CONNECTIONS_PER_ROUTE: Setting<Int> = Setting.intSetting(
        MAX_CONNECTIONS_PER_ROUTE_KEY,
        defaultSettings[MAX_CONNECTIONS_PER_ROUTE_KEY]!!.toInt(),
        NodeScope,
        Dynamic
    )

    val CONNECTION_TIMEOUT_MILLISECONDS: Setting<Int> = Setting.intSetting(
        CONNECTION_TIMEOUT_MILLISECONDS_KEY,
        defaultSettings[CONNECTION_TIMEOUT_MILLISECONDS_KEY]!!.toInt(),
        NodeScope,
        Dynamic
    )

    val SOCKET_TIMEOUT_MILLISECONDS: Setting<Int> = Setting.intSetting(
        SOCKET_TIMEOUT_MILLISECONDS_KEY,
        defaultSettings[SOCKET_TIMEOUT_MILLISECONDS_KEY]!!.toInt(),
        NodeScope,
        Dynamic
    )

    val ALLOWED_CONFIG_TYPES: Setting<List<String>> = Setting.listSetting(
        ALLOWED_CONFIG_TYPE_KEY,
        DEFAULT_ALLOWED_CONFIG_TYPES,
        { it },
        NodeScope,
        Dynamic
    )

    val TOOLTIP_SUPPORT: Setting<Boolean> = Setting.boolSetting(
        TOOLTIP_SUPPORT_KEY,
        defaultSettings[TOOLTIP_SUPPORT_KEY]!!.toBoolean(),
        NodeScope,
        Dynamic
    )

    val LEGACY_ALERTING_HOST_DENY_LIST: Setting<List<String>> = Setting.listSetting(
        LEGACY_ALERTING_HOST_DENY_LIST_KEY,
        DEFAULT_HOST_DENY_LIST,
        { it },
        NodeScope,
        Final,
        Deprecated
    )

    val ALERTING_HOST_DENY_LIST: Setting<List<String>> = Setting.listSetting(
        ALERTING_HOST_DENY_LIST_KEY,
        LEGACY_ALERTING_HOST_DENY_LIST,
        { it },
        NodeScope,
        Final
    )

    val HOST_DENY_LIST: Setting<List<String>> = Setting.listSetting(
        HOST_DENY_LIST_KEY,
        ALERTING_HOST_DENY_LIST,
        { it },
        NodeScope,
        Dynamic
    )

    private val LEGACY_EMAIL_USERNAME: Setting.AffixSetting<SecureString> = Setting.affixKeySetting(
        LEGACY_EMAIL_DESTINATION_SETTING_PREFIX,
        "username",
        { key: String -> SecureSetting.secureString(key, null) }
    )

    private val LEGACY_EMAIL_PASSWORD: Setting.AffixSetting<SecureString> = Setting.affixKeySetting(
        LEGACY_EMAIL_DESTINATION_SETTING_PREFIX,
        "password",
        { key: String -> SecureSetting.secureString(key, null) }
    )

    private val EMAIL_USERNAME: Setting.AffixSetting<SecureString> = Setting.affixKeySetting(
        EMAIL_DESTINATION_SETTING_PREFIX,
        "username",
        { key: String ->
            SecureSetting.secureString(
                key,
                fallback(key, LEGACY_EMAIL_USERNAME, "opensearch\\.notifications\\.core", "plugins.alerting.destination")
            )
        }
    )

    private val EMAIL_PASSWORD: Setting.AffixSetting<SecureString> = Setting.affixKeySetting(
        EMAIL_DESTINATION_SETTING_PREFIX,
        "password",
        { key: String ->
            SecureSetting.secureString(
                key,
                fallback(key, LEGACY_EMAIL_PASSWORD, "opensearch\\.notifications\\.core", "plugins.alerting.destination")
            )
        }
    )

    /**
     * Returns list of additional settings available specific to this plugin.
     *
     * @return list of settings defined in this plugin
     */
    fun getAllSettings(): List<Setting<*>> {
        return listOf(
            EMAIL_SIZE_LIMIT,
            EMAIL_MINIMUM_HEADER_LENGTH,
            MAX_CONNECTIONS,
            MAX_CONNECTIONS_PER_ROUTE,
            CONNECTION_TIMEOUT_MILLISECONDS,
            SOCKET_TIMEOUT_MILLISECONDS,
            ALLOWED_CONFIG_TYPES,
            TOOLTIP_SUPPORT,
            HOST_DENY_LIST,
            EMAIL_USERNAME,
            EMAIL_PASSWORD
        )
    }

    /**
     * Update the setting variables to setting values from local settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromLocal(clusterService: ClusterService) {
        allowedConfigTypes = ALLOWED_CONFIG_TYPES.get(clusterService.settings)
        emailSizeLimit = EMAIL_SIZE_LIMIT.get(clusterService.settings)
        emailMinimumHeaderLength = EMAIL_MINIMUM_HEADER_LENGTH.get(clusterService.settings)
        maxConnections = MAX_CONNECTIONS.get(clusterService.settings)
        maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE.get(clusterService.settings)
        connectionTimeout = CONNECTION_TIMEOUT_MILLISECONDS.get(clusterService.settings)
        socketTimeout = SOCKET_TIMEOUT_MILLISECONDS.get(clusterService.settings)
        tooltipSupport = TOOLTIP_SUPPORT.get(clusterService.settings)
        hostDenyList = HOST_DENY_LIST.get(clusterService.settings)
        destinationSettings = loadDestinationSettings(clusterService.settings)
        clusterName = clusterService.clusterName.value()
    }

    /**
     * Update the setting variables to setting values from cluster settings
     * @param clusterService cluster service instance
     */
    @Suppress("LongMethod")
    private fun updateSettingValuesFromCluster(clusterService: ClusterService) {
        val clusterEmailSizeLimit = clusterService.clusterSettings.get(EMAIL_SIZE_LIMIT)
        if (clusterEmailSizeLimit != null) {
            log.debug("$LOG_PREFIX:$EMAIL_SIZE_LIMIT_KEY -autoUpdatedTo-> $clusterEmailSizeLimit")
            emailSizeLimit = clusterEmailSizeLimit
        }
        val clusterEmailMinimumHeaderLength = clusterService.clusterSettings.get(EMAIL_MINIMUM_HEADER_LENGTH)
        if (clusterEmailMinimumHeaderLength != null) {
            log.debug("$LOG_PREFIX:$EMAIL_MINIMUM_HEADER_LENGTH_KEY -autoUpdatedTo-> $clusterEmailMinimumHeaderLength")
            emailMinimumHeaderLength = clusterEmailMinimumHeaderLength
        }
        val clusterMaxConnections = clusterService.clusterSettings.get(MAX_CONNECTIONS)
        if (clusterMaxConnections != null) {
            log.debug("$LOG_PREFIX:$MAX_CONNECTIONS_KEY -autoUpdatedTo-> $clusterMaxConnections")
            maxConnections = clusterMaxConnections
        }
        val clusterMaxConnectionsPerRoute = clusterService.clusterSettings.get(MAX_CONNECTIONS_PER_ROUTE)
        if (clusterMaxConnectionsPerRoute != null) {
            log.debug("$LOG_PREFIX:$MAX_CONNECTIONS_PER_ROUTE_KEY -autoUpdatedTo-> $clusterMaxConnectionsPerRoute")
            maxConnectionsPerRoute = clusterMaxConnectionsPerRoute
        }
        val clusterConnectionTimeout = clusterService.clusterSettings.get(CONNECTION_TIMEOUT_MILLISECONDS)
        if (clusterConnectionTimeout != null) {
            log.debug("$LOG_PREFIX:$CONNECTION_TIMEOUT_MILLISECONDS_KEY -autoUpdatedTo-> $clusterConnectionTimeout")
            connectionTimeout = clusterConnectionTimeout
        }
        val clusterSocketTimeout = clusterService.clusterSettings.get(SOCKET_TIMEOUT_MILLISECONDS)
        if (clusterSocketTimeout != null) {
            log.debug("$LOG_PREFIX:$SOCKET_TIMEOUT_MILLISECONDS_KEY -autoUpdatedTo-> $clusterSocketTimeout")
            socketTimeout = clusterSocketTimeout
        }
        val clusterAllowedConfigTypes = clusterService.clusterSettings.get(ALLOWED_CONFIG_TYPES)
        if (clusterAllowedConfigTypes != null) {
            log.debug("$LOG_PREFIX:$ALLOWED_CONFIG_TYPE_KEY -autoUpdatedTo-> $clusterAllowedConfigTypes")
            allowedConfigTypes = clusterAllowedConfigTypes
        }
        val clusterTooltipSupport = clusterService.clusterSettings.get(TOOLTIP_SUPPORT)
        if (clusterTooltipSupport != null) {
            log.debug("$LOG_PREFIX:$TOOLTIP_SUPPORT_KEY -autoUpdatedTo-> $clusterTooltipSupport")
            tooltipSupport = clusterTooltipSupport
        }
        val clusterHostDenyList = clusterService.clusterSettings.get(HOST_DENY_LIST)
        if (clusterHostDenyList != null) {
            log.debug("$LOG_PREFIX:$HOST_DENY_LIST_KEY -autoUpdatedTo-> $clusterHostDenyList")
            hostDenyList = clusterHostDenyList
        }
        val clusterClusterName = clusterService.clusterName
        if (clusterClusterName != null) {
            log.debug("$LOG_PREFIX:$CLUSTER_NAME_SETTING -autoUpdatedTo-> $clusterClusterName")
            clusterName = clusterClusterName.value()
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

        clusterService.clusterSettings.addSettingsUpdateConsumer(ALLOWED_CONFIG_TYPES) {
            allowedConfigTypes = it
            log.info("$LOG_PREFIX:$ALLOWED_CONFIG_TYPE_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_SIZE_LIMIT) {
            emailSizeLimit = it
            log.info("$LOG_PREFIX:$EMAIL_SIZE_LIMIT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(EMAIL_MINIMUM_HEADER_LENGTH) {
            emailMinimumHeaderLength = it
            log.info("$LOG_PREFIX:$EMAIL_MINIMUM_HEADER_LENGTH_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(MAX_CONNECTIONS) {
            maxConnections = it
            log.info("$LOG_PREFIX:$MAX_CONNECTIONS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(MAX_CONNECTIONS_PER_ROUTE) {
            maxConnectionsPerRoute = it
            log.info("$LOG_PREFIX:$MAX_CONNECTIONS_PER_ROUTE_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(CONNECTION_TIMEOUT_MILLISECONDS) {
            connectionTimeout = it
            log.info("$LOG_PREFIX:$CONNECTION_TIMEOUT_MILLISECONDS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(SOCKET_TIMEOUT_MILLISECONDS) {
            socketTimeout = it
            log.info("$LOG_PREFIX:$SOCKET_TIMEOUT_MILLISECONDS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(TOOLTIP_SUPPORT) {
            tooltipSupport = it
            log.info("$LOG_PREFIX:$TOOLTIP_SUPPORT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(HOST_DENY_LIST) {
            hostDenyList = it
            log.info("$LOG_PREFIX:$HOST_DENY_LIST_KEY -updatedTo-> $it")
        }
    }

    fun loadDestinationSettings(settings: Settings): Map<String, SecureDestinationSettings> {
        // Only loading Email Destination settings for now since those are the only secure settings needed.
        // If this logic needs to be expanded to support other Destinations, different groups can be retrieved similar
        // to emailAccountNames based on the setting namespace and SecureDestinationSettings should be expanded to support
        // these new settings.
        var emailAccountNames: Set<String> = settings.getGroups(EMAIL_DESTINATION_SETTING_PREFIX, true).keys
        // Retrieve the email account names defined under the legacy Alerting prefix as well, otherwise the fallback setting won't be checked for them
        val legacyEmailAccountNames: Set<String> = settings.getGroups(LEGACY_EMAIL_DESTINATION_SETTING_PREFIX, true).keys
        emailAccountNames = emailAccountNames.union(legacyEmailAccountNames)
        val emailAccounts: MutableMap<String, SecureDestinationSettings> = mutableMapOf()
        for (emailAccountName in emailAccountNames) {
            // Only adding the settings if they exist
            getSecureDestinationSettings(settings, emailAccountName)?.let {
                emailAccounts[emailAccountName] = it
            }
        }

        return emailAccounts
    }

    private fun getSecureDestinationSettings(settings: Settings, emailAccountName: String): SecureDestinationSettings? {
        // Using 'use' to emulate Java's try-with-resources on multiple closeable resources.
        // Values are cloned so that we maintain a SecureString, the original SecureStrings will be closed after
        // they have left the scope of this function.
        return getEmailSettingValue(settings, emailAccountName, EMAIL_USERNAME)?.use { emailUsername ->
            getEmailSettingValue(settings, emailAccountName, EMAIL_PASSWORD)?.use { emailPassword ->
                SecureDestinationSettings(emailUsername = emailUsername.clone(), emailPassword = emailPassword.clone())
            }
        }
    }

    private fun <T> getEmailSettingValue(settings: Settings, emailAccountName: String, emailSetting: Setting.AffixSetting<T>): T? {
        val concreteSetting = emailSetting.getConcreteSettingForNamespace(emailAccountName)
        return concreteSetting.get(settings)
    }

    private fun <T> fallback(key: String, affixSetting: Setting.AffixSetting<T>, regex: String, replacement: String): Setting<T>? {
        return if ("_na_" == key) {
            affixSetting.getConcreteSettingForNamespace(key)
        } else {
            affixSetting.getConcreteSetting(key.replace(regex.toRegex(), replacement))
        }
    }

    // reset the settings values to default values for testing purpose
    @OpenForTesting
    fun reset() {
        emailSizeLimit = DEFAULT_EMAIL_SIZE_LIMIT
        emailMinimumHeaderLength = DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH
        maxConnections = DEFAULT_MAX_CONNECTIONS
        maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE
        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS
        socketTimeout = DEFAULT_SOCKET_TIMEOUT_MILLISECONDS
        allowedConfigTypes = DEFAULT_ALLOWED_CONFIG_TYPES
        tooltipSupport = DEFAULT_TOOLTIP_SUPPORT
        hostDenyList = DEFAULT_HOST_DENY_LIST
    }
}
