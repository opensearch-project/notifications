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

package com.amazon.opendistroforelasticsearch.notifications

import com.amazon.opendistroforelasticsearch.commons.notifications.action.NotificationsActions
import com.amazon.opendistroforelasticsearch.notifications.action.CreateNotificationConfigAction
import com.amazon.opendistroforelasticsearch.notifications.action.DeleteNotificationConfigAction
import com.amazon.opendistroforelasticsearch.notifications.action.GetFeatureChannelListAction
import com.amazon.opendistroforelasticsearch.notifications.action.GetNotificationConfigAction
import com.amazon.opendistroforelasticsearch.notifications.action.SendMessageAction
import com.amazon.opendistroforelasticsearch.notifications.action.SendNotificationAction
import com.amazon.opendistroforelasticsearch.notifications.action.UpdateNotificationConfigAction
import com.amazon.opendistroforelasticsearch.notifications.resthandler.NotificationConfigRestHandler
import com.amazon.opendistroforelasticsearch.notifications.resthandler.SendMessageRestHandler
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import com.amazon.opendistroforelasticsearch.notifications.throttle.Accountant
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionResponse
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver
import org.elasticsearch.cluster.node.DiscoveryNodes
import org.elasticsearch.cluster.service.ClusterService
import org.elasticsearch.common.io.stream.NamedWriteableRegistry
import org.elasticsearch.common.settings.ClusterSettings
import org.elasticsearch.common.settings.IndexScopedSettings
import org.elasticsearch.common.settings.Setting
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.settings.SettingsFilter
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.env.Environment
import org.elasticsearch.env.NodeEnvironment
import org.elasticsearch.plugins.ActionPlugin
import org.elasticsearch.plugins.Plugin
import org.elasticsearch.repositories.RepositoriesService
import org.elasticsearch.rest.RestController
import org.elasticsearch.rest.RestHandler
import org.elasticsearch.script.ScriptService
import org.elasticsearch.threadpool.ThreadPool
import org.elasticsearch.watcher.ResourceWatcherService
import java.util.function.Supplier

/**
 * Entry point of the OpenDistro for Elasticsearch Notifications plugin
 * This class initializes the rest handlers.
 */
internal class NotificationPlugin : ActionPlugin, Plugin() {

    lateinit var clusterService: ClusterService // initialized in createComponents()

    internal companion object {
        private val log by logger(NotificationPlugin::class.java)

        const val PLUGIN_NAME = "opendistro-notifications"
        const val LOG_PREFIX = "notifications"
        const val PLUGIN_BASE_URI = "/_opendistro/_notifications"
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
