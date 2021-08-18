/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.notifications

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionResponse
import org.opensearch.client.Client
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.node.DiscoveryNodes
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.IndexScopedSettings
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings
import org.opensearch.common.settings.SettingsFilter
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.logger
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.notifications.action.CreateNotificationConfigAction
import org.opensearch.notifications.action.DeleteNotificationConfigAction
import org.opensearch.notifications.action.GetFeatureChannelListAction
import org.opensearch.notifications.action.GetNotificationConfigAction
import org.opensearch.notifications.action.GetNotificationEventAction
import org.opensearch.notifications.action.GetPluginFeaturesAction
import org.opensearch.notifications.action.PublishNotificationAction
import org.opensearch.notifications.action.SendNotificationAction
import org.opensearch.notifications.action.UpdateNotificationConfigAction
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.notifications.index.EventIndexingActions
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.notifications.index.NotificationEventIndex
import org.opensearch.notifications.resthandler.NotificationConfigRestHandler
import org.opensearch.notifications.resthandler.NotificationEventRestHandler
import org.opensearch.notifications.resthandler.NotificationFeatureChannelListRestHandler
import org.opensearch.notifications.resthandler.NotificationFeaturesRestHandler
import org.opensearch.notifications.resthandler.NotificationStatsRestHandler
import org.opensearch.notifications.resthandler.SendTestMessageRestHandler
import org.opensearch.notifications.security.UserAccessManager
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.plugins.ActionPlugin
import org.opensearch.plugins.Plugin
import org.opensearch.plugins.ReloadablePlugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.rest.RestController
import org.opensearch.rest.RestHandler
import org.opensearch.script.ScriptService
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier

/**
 * Entry point of the OpenSearch Notifications plugin
 * This class initializes the rest handlers.
 */
internal class NotificationPlugin : ActionPlugin, ReloadablePlugin, Plugin() {

    lateinit var clusterService: ClusterService // initialized in createComponents()

    internal companion object {
        private val log by logger(NotificationPlugin::class.java)

        const val PLUGIN_NAME = "opensearch-notifications"
        const val LOG_PREFIX = "notifications"
        const val PLUGIN_BASE_URI = "/_plugins/_notifications"
    }

    /**
     * {@inheritDoc}
     */
    override fun getSettings(): List<Setting<*>> {
        log.debug("$LOG_PREFIX:getSettings")
        return PluginSettings.getAllSettings() + org.opensearch.notifications.spi.setting.SpiSettings.getAllSettings()
    }

    /**
     * {@inheritDoc}
     */
    override fun createComponents(
        client: Client,
        clusterService: ClusterService,
        threadPool: ThreadPool,
        resourceWatcherService: ResourceWatcherService,
        scriptService: ScriptService,
        xContentRegistry: NamedXContentRegistry,
        environment: Environment,
        nodeEnvironment: NodeEnvironment,
        namedWriteableRegistry: NamedWriteableRegistry,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        repositoriesServiceSupplier: Supplier<RepositoriesService>
    ): Collection<Any> {
        log.info("zhongnan main $LOG_PREFIX:createComponents")
        this.clusterService = clusterService
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        org.opensearch.notifications.spi.setting.SpiSettings.addSettingsUpdateConsumer(clusterService)
        log.info("zhongnan main $LOG_PREFIX:createComponents:called spi load destination setting")
        NotificationConfigIndex.initialize(client, clusterService)
        NotificationEventIndex.initialize(client, clusterService)
        ConfigIndexingActions.initialize(NotificationConfigIndex, UserAccessManager)
        SendMessageActionHelper.initialize(NotificationConfigIndex, NotificationEventIndex, UserAccessManager)
        EventIndexingActions.initialize(NotificationEventIndex, UserAccessManager)
        return listOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun getActions(): List<ActionPlugin.ActionHandler<out ActionRequest, out ActionResponse>> {
        log.debug("$LOG_PREFIX:getActions")
        return listOf(
            ActionPlugin.ActionHandler(
                NotificationsActions.CREATE_NOTIFICATION_CONFIG_ACTION_TYPE,
                CreateNotificationConfigAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE,
                UpdateNotificationConfigAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.DELETE_NOTIFICATION_CONFIG_ACTION_TYPE,
                DeleteNotificationConfigAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.GET_NOTIFICATION_CONFIG_ACTION_TYPE,
                GetNotificationConfigAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.GET_NOTIFICATION_EVENT_ACTION_TYPE,
                GetNotificationEventAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.GET_FEATURE_CHANNEL_LIST_ACTION_TYPE,
                GetFeatureChannelListAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.GET_PLUGIN_FEATURES_ACTION_TYPE,
                GetPluginFeaturesAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE,
                SendNotificationAction::class.java
            ),
            ActionPlugin.ActionHandler(
                NotificationsActions.LEGACY_PUBLISH_NOTIFICATION_ACTION_TYPE,
                PublishNotificationAction::class.java
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getRestHandlers(
        settings: Settings,
        restController: RestController,
        clusterSettings: ClusterSettings,
        indexScopedSettings: IndexScopedSettings,
        settingsFilter: SettingsFilter,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        nodesInCluster: Supplier<DiscoveryNodes>
    ): List<RestHandler> {
        log.debug("$LOG_PREFIX:getRestHandlers")
        return listOf(
            NotificationConfigRestHandler(),
            NotificationEventRestHandler(),
            NotificationFeaturesRestHandler(),
            NotificationFeatureChannelListRestHandler(),
            SendTestMessageRestHandler(),
            NotificationStatsRestHandler()
        )
    }

    override fun reload(setting: Settings) {
        log.info("zhongnan notification main reload")
        org.opensearch.notifications.spi.setting.SpiSettings.destinationSettings =
            org.opensearch.notifications.spi.setting.SpiSettings.loadDestinationSettings(setting)
    }
}
