/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.CheckedRunnable
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Settings
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.NamedWriteableRegistry
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.PluginSubject
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.util.PluginClient
import org.opensearch.repositories.RepositoriesService
import org.opensearch.script.ScriptService
import org.opensearch.test.client.NoOpClient
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.security.Principal
import java.util.concurrent.TimeUnit

internal class NotificationPluginTests {

    companion object {
        private const val SUBJECT_TRANSIENT = "subject.marker"

        private val TEST_ACTION: ActionType<TestResponse> =
            ActionType("cluster:admin/notifications/plugin_wiring_test", Writeable.Reader { TestResponse() })
    }

    private lateinit var threadPool: ThreadPool
    private lateinit var delegate: RecordingClient

    @BeforeEach
    fun setup() {
        threadPool = TestThreadPool(NotificationPluginTests::class.java.simpleName)
        delegate = RecordingClient(threadPool)
    }

    @AfterEach
    fun teardown() {
        PluginSettings.reset()
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `test assignSubject gives the plugin's own client the subject`() {
        val plugin = NotificationPlugin()
        val pluginClient = createComponents(plugin).filterIsInstance<PluginClient>().single()

        plugin.assignSubject(MarkingSubject(threadPool))
        pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({}, {}))

        assertEquals("set-by-subject", delegate.subjectSeen, "the action must run as the subject assigned to the plugin")
        val indexClient = NotificationConfigIndex::class.java.getDeclaredField("client").apply { isAccessible = true }.get(null)
        assertSame(pluginClient, indexClient, "the config index must use the client that receives the subject")
    }

    private fun createComponents(plugin: NotificationPlugin): Collection<Any> {
        val clusterService = mock(ClusterService::class.java)
        whenever(clusterService.settings).thenReturn(Settings.EMPTY)
        whenever(clusterService.clusterSettings).thenReturn(ClusterSettings(Settings.EMPTY, PluginSettings.getAllSettings().toSet()))
        val environment = mock(Environment::class.java)
        whenever(environment.settings()).thenReturn(Settings.EMPTY)
        return plugin.createComponents(
            delegate,
            clusterService,
            threadPool,
            mock(ResourceWatcherService::class.java),
            mock(ScriptService::class.java),
            NamedXContentRegistry.EMPTY,
            environment,
            mock(NodeEnvironment::class.java),
            mock(NamedWriteableRegistry::class.java),
            mock(IndexNameExpressionResolver::class.java),
            { mock(RepositoriesService::class.java) }
        )
    }

    /** Stashes around its body as the real subjects do, and marks the context it runs the action in. */
    private class MarkingSubject(private val threadPool: ThreadPool) : PluginSubject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin:notifications")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            threadPool.threadContext.stashContext().use {
                threadPool.threadContext.putTransient(SUBJECT_TRANSIENT, "set-by-subject")
                runnable.run()
            }
        }
    }

    /** Records the context each action ran in and never completes it. */
    private class RecordingClient(threadPool: ThreadPool) : NoOpClient(threadPool) {
        var subjectSeen: String? = null

        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            subjectSeen = threadPool().threadContext.getTransient(SUBJECT_TRANSIENT)
        }
    }

    private class TestRequest : ActionRequest() {
        override fun validate(): ActionRequestValidationException? = null
    }

    private class TestResponse : ActionResponse() {
        override fun writeTo(out: StreamOutput) {
            // nothing in this test crosses the wire
        }
    }
}
