/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core

import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings
import org.opensearch.core.common.io.stream.NamedWriteableRegistry
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.setting.PluginSettings.loadDestinationSettings
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.NotificationCoreExtension
import org.opensearch.plugins.ExtensiblePlugin
import org.opensearch.plugins.ExtensiblePlugin.ExtensionLoader
import org.opensearch.plugins.Plugin
import org.opensearch.plugins.ReloadablePlugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.script.ScriptService
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.Client
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier

/**
 *  This is a plugin that has all send notifications functionalities
 */
class NotificationCorePlugin :
    Plugin(),
    ReloadablePlugin,
    ExtensiblePlugin {
    lateinit var clusterService: ClusterService // initialized in createComponents()

    internal companion object {
        private val log by logger(NotificationCorePlugin::class.java)

        const val PLUGIN_NAME = "opensearch-notifications-core"
        const val LOG_PREFIX = "notifications-core"
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
        repositoriesServiceSupplier: Supplier<RepositoriesService>,
    ): Collection<Any> {
        log.debug("$LOG_PREFIX:createComponents")
        this.clusterService = clusterService
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        return listOf()
    }

    override fun reload(settings: Settings) {
        PluginSettings.destinationSettings = loadDestinationSettings(settings)
    }

    override fun loadExtensions(loader: ExtensionLoader) {
        log.debug("$LOG_PREFIX:load extension")
        for (extension in loader.loadExtensions(NotificationCoreExtension::class.java)) {
            extension.setNotificationCore(NotificationCoreImpl)
        }
    }
}
