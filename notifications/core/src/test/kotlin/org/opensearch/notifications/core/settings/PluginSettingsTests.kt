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

internal class PluginSettingsTests {
    private lateinit var plugin: NotificationCorePlugin
    private lateinit var clusterService: ClusterService

    private val keyPrefix = "opensearch.notifications.core"
    private val emailKeyPrefix = "$keyPrefix.email"
    private val httpKeyPrefix = "$keyPrefix.http"
    private val emailSizeLimitKey = "$emailKeyPrefix.sizeLimit"
    private val emailMinHeaderLengthKey = "$emailKeyPrefix.minimumHeaderLength"
    private val httpMaxConnectionKey = "$httpKeyPrefix.maxConnections"
    private val httpMaxConnectionPerRouteKey = "$httpKeyPrefix.maxConnectionPerRoute"
    private val httpConnectionTimeoutKey = "$httpKeyPrefix.connectionTimeout"
    private val httpSocketTimeoutKey = "$httpKeyPrefix.socketTimeout"
    private val httpHostDenyListKey = "$httpKeyPrefix.hostDenyList"
    private val allowedConfigTypeKey = "$keyPrefix.allowedConfigTypes"
    private val allowedConfigFeatureKey = "$keyPrefix.allowedConfigFeatures"
    private val tooltipSupportKey = "$keyPrefix.tooltipSupport"

    private val defaultSettings = Settings.builder()
        .put(emailSizeLimitKey, 10000000)
        .put(emailMinHeaderLengthKey, 160)
        .put(httpMaxConnectionKey, 60)
        .put(httpMaxConnectionPerRouteKey, 20)
        .put(httpConnectionTimeoutKey, 5000)
        .put(httpSocketTimeoutKey, 50000)
        .putList(httpHostDenyListKey, listOf("localhost", "127.0.0.1"))
        .putList(
            allowedConfigTypeKey,
            listOf(
                "slack",
                "chime",
                "webhook",
                "email",
                "sns",
                "ses_account",
                "smtp_account",
                "email_group"
            )
        )
        .putList(
            allowedConfigFeatureKey,
            listOf(
                "alerting",
                "index_management",
                "reports"
            )
        )
        .put(tooltipSupportKey, true)
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
            defaultSettings[emailSizeLimitKey],
            PluginSettings.emailSizeLimit.toString()
        )
        Assertions.assertEquals(
            defaultSettings[emailMinHeaderLengthKey],
            PluginSettings.emailMinimumHeaderLength.toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpMaxConnectionKey],
            PluginSettings.maxConnections.toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpMaxConnectionPerRouteKey],
            PluginSettings.maxConnectionsPerRoute.toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpSocketTimeoutKey],
            PluginSettings.socketTimeout.toString()
        )
        Assertions.assertEquals(
            defaultSettings[allowedConfigTypeKey],
            PluginSettings.allowedConfigTypes.toString()
        )
        Assertions.assertEquals(
            defaultSettings[allowedConfigFeatureKey],
            PluginSettings.allowedConfigFeatures.toString()
        )
        Assertions.assertEquals(
            defaultSettings[tooltipSupportKey],
            PluginSettings.tooltipSupport.toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpHostDenyListKey],
            PluginSettings.hostDenyList.toString()
        )
    }

    @Test
    fun `test update settings should take cluster settings if available`() {
        val clusterSettings = Settings.builder()
            .put(emailSizeLimitKey, 20000)
            .put(emailMinHeaderLengthKey, 100)
            .put(httpMaxConnectionKey, 100)
            .put(httpMaxConnectionPerRouteKey, 100)
            .put(httpConnectionTimeoutKey, 100)
            .put(httpSocketTimeoutKey, 100)
            .putList(httpHostDenyListKey, listOf("sample"))
            .putList(allowedConfigTypeKey, listOf("slack"))
            .putList(allowedConfigFeatureKey, listOf("alerting"))
            .put(tooltipSupportKey, false)
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
            defaultSettings[emailSizeLimitKey],
            clusterService.clusterSettings.get(PluginSettings.EMAIL_SIZE_LIMIT).toString()
        )
        Assertions.assertEquals(
            defaultSettings[emailMinHeaderLengthKey],
            clusterService.clusterSettings.get(PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH).toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpMaxConnectionKey],
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS).toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpMaxConnectionPerRouteKey],
            clusterService.clusterSettings.get(PluginSettings.MAX_CONNECTIONS_PER_ROUTE).toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpConnectionTimeoutKey],
            clusterService.clusterSettings.get(PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS).toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpHostDenyListKey],
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST).toString()
        )
        Assertions.assertEquals(
            defaultSettings[allowedConfigTypeKey],
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_TYPES).toString()
        )
        Assertions.assertEquals(
            defaultSettings[allowedConfigFeatureKey],
            clusterService.clusterSettings.get(PluginSettings.ALLOWED_CONFIG_FEATURES).toString()
        )
        Assertions.assertEquals(
            defaultSettings[tooltipSupportKey],
            clusterService.clusterSettings.get(PluginSettings.TOOLTIP_SUPPORT).toString()
        )
    }
}
