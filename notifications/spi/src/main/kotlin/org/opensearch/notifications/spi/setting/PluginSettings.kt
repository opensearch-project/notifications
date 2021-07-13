/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.spi.setting

import org.opensearch.bootstrap.BootstrapInfo
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Setting.Property.Dynamic
import org.opensearch.common.settings.Setting.Property.NodeScope
import org.opensearch.common.settings.Settings
import org.opensearch.notifications.spi.NotificationSpiPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.spi.NotificationSpiPlugin.Companion.PLUGIN_NAME
import org.opensearch.notifications.spi.utils.logger
import java.io.IOException
import java.nio.file.Path

internal object PluginSettings {
    /**
     * Settings Key prefix for this plugin.
     */
    private const val KEY_PREFIX = "opensearch.notifications.spi"

    /**
     * Settings Key prefix for Email.
     */
    private const val EMAIL_KEY_PREFIX = "$KEY_PREFIX.email"

    /**
     * Settings Key prefix for http connection.
     */
    private const val HTTP_CONNECTION_KEY_PREFIX = "$KEY_PREFIX.http"

    /**
     * Email size limit.
     */
    private const val EMAIL_SIZE_LIMIT_KEY = "$EMAIL_KEY_PREFIX.sizeLimit"

    /**
     * Email minimum header length.
     */
    private const val EMAIL_MINIMUM_HEADER_LENGTH_KEY = "$EMAIL_KEY_PREFIX.minimumHeaderLength"

    /**
     * Settings Key prefix for http connection.
     */
    private const val MAX_CONNECTIONS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.maxConnections"

    /**
     * Settings Key prefix for max http connection per route.
     */
    private const val MAX_CONNECTIONS_PER_ROUTE_KEY = "$HTTP_CONNECTION_KEY_PREFIX.maxConnectionPerRoute"

    /**
     * Settings Key prefix for connection timeout in milliseconds
     */
    private const val CONNECTION_TIMEOUT_MILLISECONDS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.connectionTimeout"

    /**
     * Settings Key prefix for socket timeout in milliseconds
     */
    private const val SOCKET_TIMEOUT_MILLISECONDS_KEY = "$HTTP_CONNECTION_KEY_PREFIX.socketTimeout"

    /**
     * Setting to choose allowed config types.
     */
    private const val ALLOWED_CONFIG_TYPE_KEY = "$KEY_PREFIX.allowedConfigTypes"

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
     * Default feature list
     */
    private val DEFAULT_ALLOWED_CONFIG_TYPES = listOf(
        "slack",
        "chime",
        "custom_webhook",
        "email",
        "smtp_account",
        "email_group"
    )

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

    private const val DECIMAL_RADIX: Int = 10

    private val log by logger(javaClass)
    val defaultSettings: Map<String, String>

    init {
        var settings: Settings? = null
        val configDirName = BootstrapInfo.getSystemProperties()?.get("opensearch.path.conf")?.toString()
        if (configDirName != null) {
            val defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "notifications.yml")
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

        defaultSettings = mapOf(
            EMAIL_SIZE_LIMIT_KEY to emailSizeLimit.toString(DECIMAL_RADIX),
            EMAIL_MINIMUM_HEADER_LENGTH_KEY to emailMinimumHeaderLength.toString(DECIMAL_RADIX),
            MAX_CONNECTIONS_KEY to maxConnections.toString(DECIMAL_RADIX),
            MAX_CONNECTIONS_PER_ROUTE_KEY to maxConnectionsPerRoute.toString(DECIMAL_RADIX),
            CONNECTION_TIMEOUT_MILLISECONDS_KEY to connectionTimeout.toString(DECIMAL_RADIX),
            SOCKET_TIMEOUT_MILLISECONDS_KEY to socketTimeout.toString(DECIMAL_RADIX)
        )
    }

    private val EMAIL_SIZE_LIMIT: Setting<Int> = Setting.intSetting(
        EMAIL_SIZE_LIMIT_KEY,
        defaultSettings[EMAIL_SIZE_LIMIT_KEY]!!.toInt(),
        MINIMUM_EMAIL_SIZE_LIMIT,
        NodeScope, Dynamic
    )

    private val EMAIL_MINIMUM_HEADER_LENGTH: Setting<Int> = Setting.intSetting(
        EMAIL_MINIMUM_HEADER_LENGTH_KEY,
        defaultSettings[EMAIL_MINIMUM_HEADER_LENGTH_KEY]!!.toInt(),
        NodeScope, Dynamic
    )

    private val MAX_CONNECTIONS: Setting<Int> = Setting.intSetting(
        MAX_CONNECTIONS_KEY,
        defaultSettings[MAX_CONNECTIONS_KEY]!!.toInt(),
        NodeScope, Dynamic
    )

    private val MAX_CONNECTIONS_PER_ROUTE: Setting<Int> = Setting.intSetting(
        MAX_CONNECTIONS_PER_ROUTE_KEY,
        defaultSettings[MAX_CONNECTIONS_PER_ROUTE_KEY]!!.toInt(),
        NodeScope, Dynamic
    )

    private val CONNECTION_TIMEOUT_MILLISECONDS: Setting<Int> = Setting.intSetting(
        CONNECTION_TIMEOUT_MILLISECONDS_KEY,
        defaultSettings[CONNECTION_TIMEOUT_MILLISECONDS_KEY]!!.toInt(),
        NodeScope, Dynamic
    )

    private val SOCKET_TIMEOUT_MILLISECONDS: Setting<Int> = Setting.intSetting(
        SOCKET_TIMEOUT_MILLISECONDS_KEY,
        defaultSettings[SOCKET_TIMEOUT_MILLISECONDS_KEY]!!.toInt(),
        NodeScope, Dynamic
    )

    private val ALLOWED_CONFIG_TYPES: Setting<List<String>> = Setting.listSetting(
        ALLOWED_CONFIG_TYPE_KEY,
        DEFAULT_ALLOWED_CONFIG_TYPES,
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
            EMAIL_SIZE_LIMIT,
            EMAIL_MINIMUM_HEADER_LENGTH,
            MAX_CONNECTIONS,
            MAX_CONNECTIONS_PER_ROUTE,
            CONNECTION_TIMEOUT_MILLISECONDS,
            SOCKET_TIMEOUT_MILLISECONDS,
            ALLOWED_CONFIG_TYPES
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
        val clusterallowedConfigTypes = clusterService.clusterSettings.get(ALLOWED_CONFIG_TYPES)
        if (clusterallowedConfigTypes != null) {
            log.debug("$LOG_PREFIX:$ALLOWED_CONFIG_TYPE_KEY -autoUpdatedTo-> $clusterallowedConfigTypes")
            allowedConfigTypes = clusterallowedConfigTypes
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
    }
}
