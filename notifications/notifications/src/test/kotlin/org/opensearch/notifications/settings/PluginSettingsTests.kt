/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Settings
import org.opensearch.notifications.NotificationPlugin

internal class PluginSettingsTests {
    private lateinit var plugin: NotificationPlugin
    private lateinit var clusterService: ClusterService

    private val keyPrefix = "opensearch.notifications"
    private val generalKeyPrefix = "$keyPrefix.general"
    private val operationTimeoutKey = "$generalKeyPrefix.operation_timeout_ms"
    private val defaultItemQueryCountKey = "$generalKeyPrefix.default_items_query_count"
    private val legacyAlertingFilterByBackendRolesKey = "opendistro.alerting.filter_by_backend_roles"
    private val alertingFilterByBackendRolesKey = "plugins.alerting.filter_by_backend_roles"
    private val filterByBackendRolesKey = "$generalKeyPrefix.filter_by_backend_roles"

    private val defaultSettings = Settings.builder()
        .put(operationTimeoutKey, 60000L)
        .put(defaultItemQueryCountKey, 100L)
        .put(filterByBackendRolesKey, false)
        .build()

    @BeforeEach
    fun setup() {
        plugin = NotificationPlugin()
        clusterService = mock(ClusterService::class.java, "clusterService")
    }

    @AfterEach
    fun reset() {
        PluginSettings.reset()
    }

    @Test
    fun `test get all settings as defaults`() {
        val settings = plugin.settings

        Assert.assertTrue(
            settings.containsAll(
                listOf<Any>(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.FILTER_BY_BACKEND_ROLES
                )
            )
        )
        Assertions.assertEquals(defaultSettings[operationTimeoutKey], PluginSettings.operationTimeoutMs.toString())
        Assertions.assertEquals(
            defaultSettings[defaultItemQueryCountKey],
            PluginSettings.defaultItemsQueryCount.toString()
        )
    }

    @Test
    fun `test update settings should take cluster settings if available`() {
        val clusterSettings = Settings.builder()
            .put(operationTimeoutKey, 50000L)
            .put(defaultItemQueryCountKey, 200)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            50000L,
            clusterService.clusterSettings.get(PluginSettings.OPERATION_TIMEOUT_MS)
        )
        Assertions.assertEquals(
            200,
            clusterService.clusterSettings.get(PluginSettings.DEFAULT_ITEMS_QUERY_COUNT)
        )
    }

    @Test
    fun `test update settings should fall back to node settings if cluster settings is not available`() {
        val clusterSettings = Settings.builder().build()
        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            defaultSettings[operationTimeoutKey],
            clusterService.clusterSettings.get(PluginSettings.OPERATION_TIMEOUT_MS).toString()
        )
        Assertions.assertEquals(
            defaultSettings[defaultItemQueryCountKey],
            clusterService.clusterSettings.get(PluginSettings.DEFAULT_ITEMS_QUERY_COUNT).toString()
        )
    }

    @Test
    fun `test filter by backend roles setting uses notifications setting`() {
        val clusterSettings = Settings.builder()
            .put(filterByBackendRolesKey, true)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.FILTER_BY_BACKEND_ROLES
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(true, PluginSettings.isRbacEnabled())
    }

    @Test
    fun `test filter by backend roles setting prioritizes notifications setting`() {
        val clusterSettings = Settings.builder()
            .put(legacyAlertingFilterByBackendRolesKey, false)
            .put(alertingFilterByBackendRolesKey, false)
            .put(filterByBackendRolesKey, true)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.FILTER_BY_BACKEND_ROLES
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(true, PluginSettings.isRbacEnabled())
    }

    @Test
    fun `test filter by backend roles setting falls back to alerting setting`() {
        val clusterSettings = Settings.builder()
            .put(legacyAlertingFilterByBackendRolesKey, false)
            .put(alertingFilterByBackendRolesKey, true)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.FILTER_BY_BACKEND_ROLES
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(true, PluginSettings.isRbacEnabled())
    }

    @Test
    fun `test filter by backend roles setting falls back to alerting legacy setting when newer settings are not set`() {
        val clusterSettings = Settings.builder()
            .put(legacyAlertingFilterByBackendRolesKey, true)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.LEGACY_ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.ALERTING_FILTER_BY_BACKEND_ROLES,
                    PluginSettings.FILTER_BY_BACKEND_ROLES
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(true, PluginSettings.isRbacEnabled())
    }
}
