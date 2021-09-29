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

package org.opensearch.notifications.settings

import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Settings
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.settings.PluginSettings.DEFAULT_ITEMS_QUERY_COUNT_KEY
import org.opensearch.notifications.settings.PluginSettings.DEFAULT_ITEMS_QUERY_COUNT_VALUE
import org.opensearch.notifications.settings.PluginSettings.DEFAULT_OPERATION_TIMEOUT_MS
import org.opensearch.notifications.settings.PluginSettings.OPERATION_TIMEOUT_MS_KEY

internal class PluginSettingsTests {
    private lateinit var plugin: NotificationPlugin
    private lateinit var clusterService: ClusterService

    private val defaultSettings = Settings.builder()
        .put(OPERATION_TIMEOUT_MS_KEY, DEFAULT_OPERATION_TIMEOUT_MS)
        .put(DEFAULT_ITEMS_QUERY_COUNT_KEY, DEFAULT_ITEMS_QUERY_COUNT_VALUE)
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
                listOf<Any> (
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT
                )
            )
        )
        assertEquals(defaultSettings[OPERATION_TIMEOUT_MS_KEY], PluginSettings.operationTimeoutMs.toString())
        assertEquals(defaultSettings[DEFAULT_ITEMS_QUERY_COUNT_KEY], PluginSettings.defaultItemsQueryCount.toString())
    }

    @Test
    fun `test update settings should take cluster settings if available`() {
        val clusterSettings = Settings.builder()
            .put(OPERATION_TIMEOUT_MS_KEY, 50000L)
            .put(DEFAULT_ITEMS_QUERY_COUNT_KEY, 200)
            .build()
        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(PluginSettings.OPERATION_TIMEOUT_MS, PluginSettings.DEFAULT_ITEMS_QUERY_COUNT)
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        assertEquals(50000L, clusterService.clusterSettings.get(PluginSettings.OPERATION_TIMEOUT_MS))
        assertEquals(200, clusterService.clusterSettings.get(PluginSettings.DEFAULT_ITEMS_QUERY_COUNT))
    }

    @Test
    fun `test update settings should fall back to node settings if cluster settings is not available`() {
        val clusterSettings = Settings.builder().build()
        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(PluginSettings.OPERATION_TIMEOUT_MS, PluginSettings.DEFAULT_ITEMS_QUERY_COUNT)
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        assertEquals(DEFAULT_OPERATION_TIMEOUT_MS, clusterService.clusterSettings.get(PluginSettings.OPERATION_TIMEOUT_MS))
        assertEquals(DEFAULT_ITEMS_QUERY_COUNT_VALUE, clusterService.clusterSettings.get(PluginSettings.DEFAULT_ITEMS_QUERY_COUNT))
    }
}
