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

package org.opensearch.notifications.spi

import org.opensearch.client.Client
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.Setting
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.plugins.Plugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.script.ScriptService
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier

/**
 *  This is a dummy plugin for SPI to load configurations
 */
internal class NotificationSpiPlugin : Plugin() {
    lateinit var clusterService: ClusterService // initialized in createComponents()

    internal companion object {
        private val log by logger(NotificationSpiPlugin::class.java)

        const val PLUGIN_NAME = "opensearch-notifications"
        const val LOG_PREFIX = "notifications"
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
        return listOf()
    }
}
