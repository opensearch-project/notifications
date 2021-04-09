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

package com.amazon.opensearch.notifications

import com.amazon.opensearch.commons.notifications.action.NotificationsActions
import com.amazon.opensearch.commons.utils.logger
import com.amazon.opensearch.notifications.action.CreateNotificationConfigAction
import com.amazon.opensearch.notifications.action.DeleteNotificationConfigAction
import com.amazon.opensearch.notifications.action.GetFeatureChannelListAction
import com.amazon.opensearch.notifications.action.GetNotificationConfigAction
import com.amazon.opensearch.notifications.action.SendMessageAction
import com.amazon.opensearch.notifications.action.SendNotificationAction
import com.amazon.opensearch.notifications.action.UpdateNotificationConfigAction
import com.amazon.opensearch.notifications.resthandler.NotificationConfigRestHandler
import com.amazon.opensearch.notifications.resthandler.SendMessageRestHandler
import com.amazon.opensearch.notifications.settings.PluginSettings
import com.amazon.opensearch.notifications.throttle.Accountant
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
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.plugins.ActionPlugin
import org.opensearch.plugins.Plugin
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
internal class NotificationPlugin : ActionPlugin, Plugin() {

    lateinit var clusterService: ClusterService // initialized in createComponents()

    internal companion object {
        private val log by logger(NotificationPlugin::class.java)

        const val PLUGIN_NAME = "opensearch-notifications"
        const val LOG_PREFIX = "notifications"
        const val PLUGIN_BASE_URI = "/_opensearch/_notifications"
    }

    /**
     * {@inheritDoc}
     */
    override fun getSettings(): List<Setting<*>> {
        log.debug("$LOG_PREFIX:getSettings")
        return PluginSettings.getAllSettings()
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
        log.debug("$LOG_PREFIX:createComponents")
        this.clusterService = clusterService
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        Accountant.initialize(client, clusterService)
        return listOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun getActions(): List<ActionPlugin.ActionHandler<out ActionRequest, out ActionResponse>> {
        log.debug("$LOG_PREFIX:getActions")
        return listOf(
            ActionPlugin.ActionHandler(SendMessageAction.ACTION_TYPE, SendMessageAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.CREATE_NOTIFICATION_CONFIG_ACTION_TYPE, CreateNotificationConfigAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.UPDATE_NOTIFICATION_CONFIG_ACTION_TYPE, UpdateNotificationConfigAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.DELETE_NOTIFICATION_CONFIG_ACTION_TYPE, DeleteNotificationConfigAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.GET_NOTIFICATION_CONFIG_ACTION_TYPE, GetNotificationConfigAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.GET_FEATURE_CHANNEL_LIST_ACTION_TYPE, GetFeatureChannelListAction::class.java),
            ActionPlugin.ActionHandler(NotificationsActions.SEND_NOTIFICATION_ACTION_TYPE, SendNotificationAction::class.java)
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
            SendMessageRestHandler(),
            NotificationConfigRestHandler()
        )
    }
}
