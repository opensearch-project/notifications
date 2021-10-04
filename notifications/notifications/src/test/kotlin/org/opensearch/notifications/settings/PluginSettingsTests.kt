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
    private val operationTimeoutKey = "$generalKeyPrefix.operationTimeoutMs"
    private val defaultItemQueryCountKey = "$generalKeyPrefix.defaultItemsQueryCount"

    private val defaultSettings = Settings.builder()
        .put(operationTimeoutKey, 60000L)
        .put(defaultItemQueryCountKey, 100L)
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
        Assertions.assertEquals(defaultSettings[operationTimeoutKey], PluginSettings.operationTimeoutMs.toString())
        Assertions.assertEquals(defaultSettings[defaultItemQueryCountKey], PluginSettings.defaultItemsQueryCount.toString())
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
                setOf(PluginSettings.OPERATION_TIMEOUT_MS, PluginSettings.DEFAULT_ITEMS_QUERY_COUNT)
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
                setOf(PluginSettings.OPERATION_TIMEOUT_MS, PluginSettings.DEFAULT_ITEMS_QUERY_COUNT)
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
}
