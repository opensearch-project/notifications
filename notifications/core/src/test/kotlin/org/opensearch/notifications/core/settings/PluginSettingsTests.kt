/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
    private val emailSizeLimitKey = "$emailKeyPrefix.size_limit"
    private val emailMinHeaderLengthKey = "$emailKeyPrefix.minimum_header_length"
    private val httpMaxConnectionKey = "$httpKeyPrefix.max_connections"
    private val httpMaxConnectionPerRouteKey = "$httpKeyPrefix.max_connection_per_route"
    private val httpConnectionTimeoutKey = "$httpKeyPrefix.connection_timeout"
    private val httpSocketTimeoutKey = "$httpKeyPrefix.socket_timeout"
    private val legacyAlertingHostDenyListKey = "opendistro.destination.host.deny_list"
    private val alertingHostDenyListKey = "plugins.destination.host.deny_list"
    private val httpHostDenyListKey = "$httpKeyPrefix.host_deny_list"
    private val allowedConfigTypeKey = "$keyPrefix.allowed_config_types"
    private val tooltipSupportKey = "$keyPrefix.tooltip_support"
    private val enableHtmlSanitizationKey = "$emailKeyPrefix.enable_html_sanitization"
    private val htmlSanitizationAllowListKey = "$emailKeyPrefix.html_sanitization_allow_list"
    private val htmlSanitizationDenyListKey = "$emailKeyPrefix.html_sanitization_deny_list"

    private val defaultSettings = Settings.builder()
        .put(emailSizeLimitKey, 10000000)
        .put(emailMinHeaderLengthKey, 160)
        .put(httpMaxConnectionKey, 60)
        .put(httpMaxConnectionPerRouteKey, 20)
        .put(httpConnectionTimeoutKey, 5000)
        .put(httpSocketTimeoutKey, 50000)
        .putList(httpHostDenyListKey, emptyList<String>())
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
        .put(tooltipSupportKey, true)
        .put(enableHtmlSanitizationKey, true)
        .putList(htmlSanitizationAllowListKey, listOf("blocks_group", "formatting_group", "images_group", "links_group", "styles_group", "tables_group"))
        .putList(htmlSanitizationDenyListKey, emptyList<String>())
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
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
            defaultSettings[tooltipSupportKey],
            PluginSettings.tooltipSupport.toString()
        )
        Assertions.assertEquals(
            defaultSettings[httpHostDenyListKey],
            PluginSettings.hostDenyList.toString()
        )
        Assertions.assertEquals(
            defaultSettings[enableHtmlSanitizationKey],
            PluginSettings.enableEmailHtmlSanitization.toString()
        )
        Assertions.assertEquals(
            defaultSettings[htmlSanitizationAllowListKey],
            PluginSettings.emailHtmlSanitizationAllowList.toString()
        )
        Assertions.assertEquals(
            defaultSettings[htmlSanitizationDenyListKey],
            PluginSettings.emailHtmlSanitizationDenyList.toString()
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
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
            false,
            clusterService.clusterSettings.get(PluginSettings.TOOLTIP_SUPPORT)
        )
        Assertions.assertEquals(
            true,
            clusterService.clusterSettings.get(PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION)
        )
        Assertions.assertEquals(
            listOf("blocks_group", "formatting_group", "images_group", "links_group", "styles_group", "tables_group"),
            clusterService.clusterSettings.get(PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST)
        )
        Assertions.assertEquals(
            emptyList<String>(),
            clusterService.clusterSettings.get(PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST)
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
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
            defaultSettings[tooltipSupportKey],
            clusterService.clusterSettings.get(PluginSettings.TOOLTIP_SUPPORT).toString()
        )
        Assertions.assertEquals(
            defaultSettings[enableHtmlSanitizationKey],
            clusterService.clusterSettings.get(PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION).toString()
        )
        Assertions.assertEquals(
            defaultSettings[htmlSanitizationAllowListKey],
            clusterService.clusterSettings.get(PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST).toString()
        )
        Assertions.assertEquals(
            defaultSettings[htmlSanitizationDenyListKey],
            clusterService.clusterSettings.get(PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST).toString()
        )
    }

    @Test
    fun `test notifications host deny list takes precedence over fallbacks`() {
        val clusterSettings = Settings.builder()
            .putList(legacyAlertingHostDenyListKey, emptyList())
            .putList(alertingHostDenyListKey, emptyList())
            .putList(httpHostDenyListKey, listOf("sample"))
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.LEGACY_ALERTING_HOST_DENY_LIST,
                    PluginSettings.ALERTING_HOST_DENY_LIST,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            listOf("sample"),
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST)
        )
    }

    @Test
    fun `test host deny list falls back to alerting setting if available`() {
        val clusterSettings = Settings.builder()
            .putList(legacyAlertingHostDenyListKey, emptyList())
            .putList(alertingHostDenyListKey, listOf("sample"))
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.LEGACY_ALERTING_HOST_DENY_LIST,
                    PluginSettings.ALERTING_HOST_DENY_LIST,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            listOf("sample"),
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST)
        )
    }

    @Test
    fun `test host deny list falls back to legacy alerting setting if newer settings are not set`() {
        val clusterSettings = Settings.builder()
            .putList(legacyAlertingHostDenyListKey, listOf("sample"))
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
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.LEGACY_ALERTING_HOST_DENY_LIST,
                    PluginSettings.ALERTING_HOST_DENY_LIST,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            listOf("sample"),
            clusterService.clusterSettings.get(PluginSettings.HOST_DENY_LIST)
        )
    }

    @Test
    fun `test html sanitization for email settings can be set at node scope`() {
        var nodeSettings = Settings.builder()
            .put(enableHtmlSanitizationKey, true)
            .putList(htmlSanitizationAllowListKey, listOf("blocks_group", "links_group"))
            .putList(htmlSanitizationDenyListKey, listOf("h1", "h2"))
            .build()

        whenever(clusterService.settings).thenReturn(nodeSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                Settings.builder().build(),
                setOf(
                    PluginSettings.EMAIL_SIZE_LIMIT,
                    PluginSettings.EMAIL_MINIMUM_HEADER_LENGTH,
                    PluginSettings.MAX_CONNECTIONS,
                    PluginSettings.MAX_CONNECTIONS_PER_ROUTE,
                    PluginSettings.CONNECTION_TIMEOUT_MILLISECONDS,
                    PluginSettings.SOCKET_TIMEOUT_MILLISECONDS,
                    PluginSettings.ALLOWED_CONFIG_TYPES,
                    PluginSettings.TOOLTIP_SUPPORT,
                    PluginSettings.LEGACY_ALERTING_HOST_DENY_LIST,
                    PluginSettings.ALERTING_HOST_DENY_LIST,
                    PluginSettings.HOST_DENY_LIST,
                    PluginSettings.ENABLE_EMAIL_HTML_SANITIZATION,
                    PluginSettings.EMAIL_HTML_SANITIZATION_ALLOW_LIST,
                    PluginSettings.EMAIL_HTML_SANITIZATION_DENY_LIST
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            true,
            PluginSettings.enableEmailHtmlSanitization
        )
        Assertions.assertEquals(
            listOf("blocks_group", "links_group"),
            PluginSettings.emailHtmlSanitizationAllowList
        )
        Assertions.assertEquals(
            listOf("h1", "h2"),
            PluginSettings.emailHtmlSanitizationDenyList
        )

        nodeSettings = Settings.builder()
            .put(enableHtmlSanitizationKey, false)
            .build()

        whenever(clusterService.settings).thenReturn(nodeSettings)
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            false,
            PluginSettings.enableEmailHtmlSanitization
        )

        nodeSettings = Settings.builder()
            .put(enableHtmlSanitizationKey, true)
            .putList(htmlSanitizationAllowListKey, emptyList())
            .putList(htmlSanitizationDenyListKey, emptyList())
            .build()

        whenever(clusterService.settings).thenReturn(nodeSettings)
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Assertions.assertEquals(
            true,
            PluginSettings.enableEmailHtmlSanitization
        )
        Assertions.assertEquals(
            emptyList<String>(),
            PluginSettings.emailHtmlSanitizationAllowList
        )
        Assertions.assertEquals(
            emptyList<String>(),
            PluginSettings.emailHtmlSanitizationDenyList
        )
    }
}
