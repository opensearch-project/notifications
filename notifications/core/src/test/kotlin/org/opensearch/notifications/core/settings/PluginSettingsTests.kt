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

package org.opensearch.notifications.core.settings

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
import org.opensearch.notifications.core.NotificationCorePlugin
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.setting.PluginSettings.ALLOWED_CONFIG_FEATURE_KEY
import org.opensearch.notifications.core.setting.PluginSettings.ALLOWED_CONFIG_TYPE_KEY
import org.opensearch.notifications.core.setting.PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS_KEY
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_ALLOWED_CONFIG_FEATURES
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_ALLOWED_CONFIG_TYPES
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_EMAIL_SIZE_LIMIT
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_HOST_DENY_LIST
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_MAX_CONNECTIONS
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_MAX_CONNECTIONS_PER_ROUTE
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_SOCKET_TIMEOUT_MILLISECONDS
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_TOOLTIP_SUPPORT
import org.opensearch.notifications.core.setting.PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH_KEY
import org.opensearch.notifications.core.setting.PluginSettings.EMAIL_SIZE_LIMIT_KEY
import org.opensearch.notifications.core.setting.PluginSettings.HOST_DENY_LIST_KEY
import org.opensearch.notifications.core.setting.PluginSettings.MAX_CONNECTIONS_KEY
import org.opensearch.notifications.core.setting.PluginSettings.MAX_CONNECTIONS_PER_ROUTE_KEY
import org.opensearch.notifications.core.setting.PluginSettings.SOCKET_TIMEOUT_MILLISECONDS_KEY
import org.opensearch.notifications.core.setting.PluginSettings.TOOLTIP_SUPPORT_KEY

internal class PluginSettingsTests {
    private lateinit var plugin: NotificationCorePlugin
    private lateinit var clusterService: ClusterService

    private val defaultSettings = Settings.builder()
        .put(EMAIL_SIZE_LIMIT_KEY, DEFAULT_EMAIL_SIZE_LIMIT)
        .put(EMAIL_MINIMUM_HEADER_LENGTH_KEY, DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH)
        .put(MAX_CONNECTIONS_KEY, DEFAULT_MAX_CONNECTIONS)
        .put(MAX_CONNECTIONS_PER_ROUTE_KEY, DEFAULT_MAX_CONNECTIONS_PER_ROUTE)
        .put(CONNECTION_TIMEOUT_MILLISECONDS_KEY, DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS)
        .put(SOCKET_TIMEOUT_MILLISECONDS_KEY, DEFAULT_SOCKET_TIMEOUT_MILLISECONDS)
        .putList(HOST_DENY_LIST_KEY, DEFAULT_HOST_DENY_LIST)
        .putList(ALLOWED_CONFIG_TYPE_KEY, DEFAULT_ALLOWED_CONFIG_TYPES)
        .putList(ALLOWED_CONFIG_FEATURE_KEY, DEFAULT_ALLOWED_CONFIG_FEATURES)
        .put(TOOLTIP_SUPPORT_KEY, DEFAULT_TOOLTIP_SUPPORT)
        .build()

    @BeforeEach
    fun setup() {
        plugin = NotificationCorePlugin()
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
                    PluginSettings.EMAIL_SIZE_LIMIT,
                    PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH,
                    PluginSettings.MAX_CONNECTIONS,
                    PluginSettings.MAX_CONNECTIONS_PER_ROUTE,
                    PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS,
                    PluginSettings.SOCKET_TIMEOUT_MILLISECONDS,
                    PluginSettings.ALLOWED_CONFIG_TYPES,
                    PluginSettings.ALLOWED_CONFIG_FEATURES,
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST
                )
            )
        )

        Assertions.assertEquals(
            defaultSettings[EMAIL_SIZE_LIMIT_KEY],
            PluginSettings.emailSizeLimit.toString()
        )
        Assertions.assertEquals(
            defaultSettings[EMAIL_MINIMUM_HEADER_LENGTH_KEY],
            PluginSettings.emailMinimumHeaderLength.toString()
        )
        Assertions.assertEquals(
            defaultSettings[MAX_CONNECTIONS_KEY],
            PluginSettings.maxConnections.toString()
        )
        Assertions.assertEquals(
            defaultSettings[MAX_CONNECTIONS_PER_ROUTE_KEY],
            PluginSettings.maxConnectionsPerRoute.toString()
        )
        Assertions.assertEquals(
            defaultSettings[SOCKET_TIMEOUT_MILLISECONDS_KEY],
            PluginSettings.socketTimeout.toString()
        )
        Assertions.assertEquals(
            defaultSettings[ALLOWED_CONFIG_TYPE_KEY],
            PluginSettings.allowedConfigTypes.toString()
        )
        Assertions.assertEquals(
            defaultSettings[ALLOWED_CONFIG_TYPE_KEY],
            PluginSettings.allowedConfigTypes.toString()
        )
        Assertions.assertEquals(
            defaultSettings[ALLOWED_CONFIG_TYPE_KEY],
            PluginSettings.allowedConfigTypes.toString()
        )
        Assertions.assertEquals(
            defaultSettings[TOOLTIP_SUPPORT_KEY],
            PluginSettings.tooltipSupport.toString()
        )
        Assertions.assertEquals(
            defaultSettings[HOST_DENY_LIST_KEY],
            PluginSettings.hostDenyList.toString()
        )
    }

    @Test
    fun `test update settings should take cluster settings if available`() {
        val clusterSettings = Settings.builder()
            .put(EMAIL_SIZE_LIMIT_KEY, 20000)
            .put(EMAIL_MINIMUM_HEADER_LENGTH_KEY, 100)
            .put(MAX_CONNECTIONS_KEY, 100)
            .put(MAX_CONNECTIONS_PER_ROUTE_KEY, 100)
            .put(CONNECTION_TIMEOUT_MILLISECONDS_KEY, 100)
            .put(SOCKET_TIMEOUT_MILLISECONDS_KEY, 100)
            .putList(HOST_DENY_LIST_KEY, listOf("sample"))
            .putList(ALLOWED_CONFIG_TYPE_KEY, listOf("slack"))
            .putList(ALLOWED_CONFIG_FEATURE_KEY, listOf("alerting"))
            .put(TOOLTIP_SUPPORT_KEY, false)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.EMAIL_SIZE_LIMIT,
                    PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH,
                    PluginSettings.MAX_CONNECTIONS,
                    PluginSettings.MAX_CONNECTIONS_PER_ROUTE,
                    PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS,
                    PluginSettings.SOCKET_TIMEOUT_MILLISECONDS,
                    PluginSettings.ALLOWED_CONFIG_TYPES,
                    PluginSettings.ALLOWED_CONFIG_FEATURES,
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            20000,
            clusterService.clusterSettings.get(PluginSettings.EMAIL_SIZE_LIMIT)
        )
        Assertions.assertEquals(
            100,
            clusterService.clusterSettings.get(PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH)
        )
        Assertions.assertEquals(
            100,
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS)
        )
        Assertions.assertEquals(
            100,
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS_PER_ROUTE)
        )
        Assertions.assertEquals(
            100,
            clusterService.clusterSettings.get(PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS)
        )
        Assertions.assertEquals(
            listOf("sample"),
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST)
        )
        Assertions.assertEquals(
            listOf("slack"),
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_TYPES)
        )
        Assertions.assertEquals(
            listOf("alerting"),
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_FEATURES)
        )
        Assertions.assertEquals(
            false,
            clusterService.clusterSettings.get(PluginSettings.TOOLTIP_SUPPORT)
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
                    PluginSettings.EMAIL_SIZE_LIMIT,
                    PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH,
                    PluginSettings.MAX_CONNECTIONS,
                    PluginSettings.MAX_CONNECTIONS_PER_ROUTE,
                    PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS,
                    PluginSettings.SOCKET_TIMEOUT_MILLISECONDS,
                    PluginSettings.ALLOWED_CONFIG_TYPES,
                    PluginSettings.ALLOWED_CONFIG_FEATURES,
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            DEFAULT_EMAIL_SIZE_LIMIT,
            clusterService.clusterSettings.get(PluginSettings.EMAIL_SIZE_LIMIT)
        )
        Assertions.assertEquals(
            DEFAULT_MINIMUM_EMAIL_HEADER_LENGTH,
            clusterService.clusterSettings.get(PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH)
        )
        Assertions.assertEquals(
            DEFAULT_MAX_CONNECTIONS,
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS)
        )
        Assertions.assertEquals(
            DEFAULT_MAX_CONNECTIONS_PER_ROUTE,
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS_PER_ROUTE)
        )
        Assertions.assertEquals(
            DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS,
            clusterService.clusterSettings.get(PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS)
        )
        Assertions.assertEquals(
            DEFAULT_HOST_DENY_LIST,
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST)
        )
        Assertions.assertEquals(
            DEFAULT_ALLOWED_CONFIG_TYPES,
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_TYPES)
        )
        Assertions.assertEquals(
            DEFAULT_ALLOWED_CONFIG_FEATURES,
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_FEATURES)
        )
        Assertions.assertEquals(
            DEFAULT_TOOLTIP_SUPPORT,
            clusterService.clusterSettings.get(PluginSettings.TOOLTIP_SUPPORT)
        )
    }
}
