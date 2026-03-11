
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.security

import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Settings
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings

internal class UserAccessManagerTests {
    private lateinit var clusterService: ClusterService

    private val keyPrefix = "opensearch.notifications"
    private val generalKeyPrefix = "$keyPrefix.general"
    private val filterByBackendRolesAccessStrategyKey = "$generalKeyPrefix.filter_by_backend_roles_access_strategy"

    private val defaultSettings = Settings.builder()
        .put(filterByBackendRolesAccessStrategyKey, FilterByBackendRolesAccessStrategy.INTERSECT.strategy)
        .build()

    @BeforeEach
    fun setup() {
        clusterService = mock(ClusterService::class.java, "clusterService")
    }

    @AfterEach
    fun reset() {
        PluginSettings.reset()
    }

    @Test
    fun `checkUserBackendRolesAccess strategy is intersect and roles are the same`() {
        Assert.assertTrue(
            UserAccessManager.checkUserBackendRolesAccess(
                listOf("role1", "role2"),
                listOf("role2", "role3")
            )
        )
    }

    @Test
    fun `checkUserBackendRolesAccess strategy is intersect and roles are different`() {
        Assert.assertFalse(
            UserAccessManager.checkUserBackendRolesAccess(
                listOf("role1"),
                listOf("role2")
            )
        )
    }

    @Test
    fun `checkUserBackendRolesAccess strategy is all and roles intersect but are not the same`() {
        val clusterSettings = Settings.builder()
            .put(filterByBackendRolesAccessStrategyKey, FilterByBackendRolesAccessStrategy.ALL.strategy)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)

        Assert.assertFalse(
            UserAccessManager.checkUserBackendRolesAccess(
                listOf("role1", "role2"),
                listOf("role2", "role3")
            )
        )
    }

    @Test
    fun `checkUserBackendRolesAccess strategy is all and roles are the same`() {
        val clusterSettings = Settings.builder()
            .put(filterByBackendRolesAccessStrategyKey, FilterByBackendRolesAccessStrategy.ALL.strategy)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)

        Assert.assertTrue(
            UserAccessManager.checkUserBackendRolesAccess(
                listOf("role1", "role2"),
                listOf("role1", "role2")
            )
        )
    }

    @Test
    fun `checkUserBackendRolesAccess strategy is all and roles are the same, but in different order`() {
        val clusterSettings = Settings.builder()
            .put(filterByBackendRolesAccessStrategyKey, FilterByBackendRolesAccessStrategy.ALL.strategy)
            .build()

        whenever(clusterService.settings).thenReturn(defaultSettings)
        whenever(clusterService.clusterSettings).thenReturn(
            ClusterSettings(
                clusterSettings,
                setOf(
                    PluginSettings.OPERATION_TIMEOUT_MS,
                    PluginSettings.DEFAULT_ITEMS_QUERY_COUNT,
                    PluginSettings.FILTER_BY_BACKEND_ROLES_ACCESS_STRATEGY
                )
            )
        )
        PluginSettings.addSettingsUpdateConsumer(clusterService)

        Assert.assertTrue(
            UserAccessManager.checkUserBackendRolesAccess(
                listOf("role2", "role1"),
                listOf("role1", "role2")
            )
        )
    }
}
