/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

import org.opensearch.bootstrap.BootstrapInfo
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Setting.Property.Deprecated
import org.opensearch.common.settings.Setting.Property.Dynamic
import org.opensearch.common.settings.Setting.Property.NodeScope
import org.opensearch.common.settings.Settings
import org.opensearch.commons.utils.OpenForTesting
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import org.opensearch.remote.metadata.common.CommonValue.REMOTE_METADATA_ENDPOINT_KEY
import org.opensearch.remote.metadata.common.CommonValue.REMOTE_METADATA_REGION_KEY
import org.opensearch.remote.metadata.common.CommonValue.REMOTE_METADATA_SERVICE_NAME_KEY
import org.opensearch.remote.metadata.common.CommonValue.REMOTE_METADATA_TYPE_KEY
import java.io.IOException
import java.nio.file.Path

/**
 * settings specific to Notifications Plugin.
 */
internal object PluginSettings {
    private lateinit var clusterService: ClusterService

    /**
     * Settings Key-prefix for this plugin.
     */
    private const val KEY_PREFIX = "opensearch.notifications"

    /**
     * General settings Key prefix.
     */
    private const val GENERAL_KEY_PREFIX = "$KEY_PREFIX.general"

    /**
     * Operation timeout for network operations.
     */
    private const val OPERATION_TIMEOUT_MS_KEY = "$GENERAL_KEY_PREFIX.operation_timeout_ms"

    /**
     * Setting to choose default number of items to query.
     */
    private const val DEFAULT_ITEMS_QUERY_COUNT_KEY = "$GENERAL_KEY_PREFIX.default_items_query_count"

    /**
     * Legacy alerting plugin filter_by_backend_roles setting.
     */
    private const val LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES_KEY = "opendistro.alerting.filter_by_backend_roles"

    /**
     * Alerting plugin filter_by_backend_roles setting.
     */
    private const val ALERTING_FILTER_BY_BACKEND_ROLES_KEY = "plugins.alerting.filter_by_backend_roles"

    /**
     * Setting to enable filtering by backend roles.
     */
    private const val FILTER_BY_BACKEND_ROLES_KEY = "$GENERAL_KEY_PREFIX.filter_by_backend_roles"

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
     * Operation timeout setting in ms for I/O operations
     */
    @Volatile
    var operationTimeoutMs: Long

    /**
     * Default number of items to query.
     */
    @Volatile
    var defaultItemsQueryCount: Int

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
            } catch (e: IOException) {
                log.warn("$LOG_PREFIX:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}:${e.message}")
            }
        }
        // Initialize the settings values to default values
        operationTimeoutMs = (settings?.get(OPERATION_TIMEOUT_MS_KEY)?.toLong()) ?: DEFAULT_OPERATION_TIMEOUT_MS
        defaultItemsQueryCount = (settings?.get(DEFAULT_ITEMS_QUERY_COUNT_KEY)?.toInt())
            ?: DEFAULT_ITEMS_QUERY_COUNT_VALUE
        defaultSettings =
            mapOf(
                OPERATION_TIMEOUT_MS_KEY to operationTimeoutMs.toString(DECIMAL_RADIX),
                DEFAULT_ITEMS_QUERY_COUNT_KEY to defaultItemsQueryCount.toString(DECIMAL_RADIX),
            )
    }

    val OPERATION_TIMEOUT_MS: Setting<Long> =
        Setting.longSetting(
            OPERATION_TIMEOUT_MS_KEY,
            defaultSettings[OPERATION_TIMEOUT_MS_KEY]!!.toLong(),
            MINIMUM_OPERATION_TIMEOUT_MS,
            NodeScope,
            Dynamic,
        )

    val DEFAULT_ITEMS_QUERY_COUNT: Setting<Int> =
        Setting.intSetting(
            DEFAULT_ITEMS_QUERY_COUNT_KEY,
            defaultSettings[DEFAULT_ITEMS_QUERY_COUNT_KEY]!!.toInt(),
            MINIMUM_ITEMS_QUERY_COUNT,
            NodeScope,
            Dynamic,
        )

    val LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES: Setting<Boolean> =
        Setting.boolSetting(
            LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES_KEY,
            false,
            NodeScope,
            Dynamic,
            Deprecated,
        )

    val ALERTING_FILTER_BY_BACKEND_ROLES: Setting<Boolean> =
        Setting.boolSetting(
            ALERTING_FILTER_BY_BACKEND_ROLES_KEY,
            LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES,
            NodeScope,
            Dynamic,
        )

    val FILTER_BY_BACKEND_ROLES: Setting<Boolean> =
        Setting.boolSetting(
            FILTER_BY_BACKEND_ROLES_KEY,
            ALERTING_FILTER_BY_BACKEND_ROLES,
            NodeScope,
            Dynamic,
        )

    /** This setting sets the remote metadata store type  */
    val REMOTE_METADATA_STORE_TYPE: Setting<String?> =
        Setting
            .simpleString(
                "plugins.alerting.$REMOTE_METADATA_TYPE_KEY",
                NodeScope,
                Setting.Property.Final,
            )

    /** This setting sets the remote metadata endpoint  */
    val REMOTE_METADATA_ENDPOINT: Setting<String?> =
        Setting
            .simpleString(
                "plugins.alerting.$REMOTE_METADATA_ENDPOINT_KEY",
                NodeScope,
                Setting.Property.Final,
            )

    /** This setting sets the remote metadata region  */
    val REMOTE_METADATA_REGION: Setting<String?> =
        Setting
            .simpleString(
                "plugins.alerting.$REMOTE_METADATA_REGION_KEY",
                NodeScope,
                Setting.Property.Final,
            )

    /** This setting sets the remote metadata service name  */
    val REMOTE_METADATA_SERVICE_NAME: Setting<String?> =
        Setting
            .simpleString(
                "plugins.alerting.$REMOTE_METADATA_SERVICE_NAME_KEY",
                NodeScope,
                Setting.Property.Final,
            )

    fun isRbacEnabled(): Boolean {
        return if (clusterService.clusterSettings.get(FILTER_BY_BACKEND_ROLES_KEY) != null) {
            return clusterService.clusterSettings.get(FILTER_BY_BACKEND_ROLES) ?: false
        } else {
            false
        }
    }

    /**
     * Returns list of additional settings available specific to this plugin.
     *
     * @return list of settings defined in this plugin
     */
    fun getAllSettings(): List<Setting<*>> =
        listOf(
            OPERATION_TIMEOUT_MS,
            DEFAULT_ITEMS_QUERY_COUNT,
            FILTER_BY_BACKEND_ROLES,
            REMOTE_METADATA_REGION,
            REMOTE_METADATA_ENDPOINT,
            REMOTE_METADATA_STORE_TYPE,
            REMOTE_METADATA_SERVICE_NAME,
        )

    /**
     * Update the setting variables to setting values from local settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromLocal(clusterService: ClusterService) {
        operationTimeoutMs = OPERATION_TIMEOUT_MS.get(clusterService.settings)
        defaultItemsQueryCount = DEFAULT_ITEMS_QUERY_COUNT.get(clusterService.settings)
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
    }

    /**
     * adds Settings update listeners to all settings.
     * @param clusterService cluster service instance
     */
    fun addSettingsUpdateConsumer(clusterService: ClusterService) {
        this.clusterService = clusterService
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
    }

    // reset the settings values to default values for testing purpose
    @OpenForTesting
    fun reset() {
        operationTimeoutMs = DEFAULT_OPERATION_TIMEOUT_MS
        defaultItemsQueryCount = DEFAULT_ITEMS_QUERY_COUNT_VALUE
    }
}
