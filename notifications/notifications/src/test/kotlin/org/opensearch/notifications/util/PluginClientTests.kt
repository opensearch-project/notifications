/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.common.CheckedRunnable
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.PluginSubject
import org.opensearch.test.client.NoOpClient
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.Client
import java.security.Principal
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal class PluginClientTests {

    companion object {
        private const val CALLER_TRANSIENT = "caller.marker"
        private const val SUBJECT_TRANSIENT = "subject.marker"
        private const val RESPONDER_TRANSIENT = "responder.marker"
        private const val NOT_RUN = "listener did not run"

        private val TEST_ACTION: ActionType<TestResponse> =
            ActionType("cluster:admin/notifications/plugin_client_test", Writeable.Reader { TestResponse() })
    }

    private lateinit var threadPool: ThreadPool
    private lateinit var threadContext: ThreadContext
    private lateinit var delegate: CapturingClient
    private lateinit var pluginClient: PluginClient
    private lateinit var responder: ExecutorService

    @BeforeEach
    fun setup() {
        threadPool = TestThreadPool(PluginClientTests::class.java.simpleName)
        threadContext = threadPool.threadContext
        delegate = CapturingClient(threadPool)
        pluginClient = PluginClient(delegate)
        responder = Executors.newSingleThreadExecutor()
    }

    @AfterEach
    fun teardown() {
        responder.shutdownNow()
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `test the action runs inside the subject`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        assignSubject()

        pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({}, {}))

        assertEquals("set-by-subject", delegate.subjectSeen, "the action must run inside runAs")
        assertNull(delegate.callerSeen, "the caller's context must not reach the action")
    }

    @Test
    fun `test the listener sees the caller context and not the subject's on response`() {
        assertListenerSeesCallerContext { it.onResponse(TestResponse()) }
    }

    @Test
    fun `test the listener sees the caller context and not the subject's on failure`() {
        assertListenerSeesCallerContext { it.onFailure(IllegalStateException("transport failure")) }
    }

    @Test
    fun `test the responding thread's own context comes back after the listener returns`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        assignSubject()
        pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({}, {}))

        // Restoring the caller's context and leaving it there would strand it on a pooled thread for whatever runs next.
        val responderAfter = AtomicReference<String?>(NOT_RUN)
        val callerLeftBehind = AtomicReference<String?>(NOT_RUN)
        completeOnAnotherThread {
            it.onResponse(TestResponse())
            responderAfter.set(threadContext.getTransient(RESPONDER_TRANSIENT))
            callerLeftBehind.set(threadContext.getTransient(CALLER_TRANSIENT))
        }

        assertEquals("set-by-responder", responderAfter.get(), "the responding thread's own context must come back")
        assertNull(callerLeftBehind.get(), "the caller's context must not be left on the responding thread")
    }

    @Test
    fun `test the listener sees a response header the action added`() {
        assignSubject()
        val warnings = AtomicReference<List<String>?>()
        pluginClient.execute(
            TEST_ACTION,
            TestRequest(),
            ActionListener.wrap({ warnings.set(threadContext.responseHeaders["Warning"]) }, {})
        )

        completeOnAnotherThread {
            threadContext.addResponseHeader("Warning", "deprecated")
            it.onResponse(TestResponse())
        }

        assertEquals(listOf("deprecated"), warnings.get())
    }

    @Test
    fun `test a synchronous delegate failure reaches the caller and not the listener`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        assignSubject()
        val boom = IllegalArgumentException("failed to find action")
        delegate.behaviour = { throw boom }

        val failures = mutableListOf<Exception>()
        val thrown = assertThrows<IllegalArgumentException> {
            pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({}, { failures.add(it) }))
        }

        assertSame(boom, thrown)
        assertEquals(emptyList<Exception>(), failures, "a failure thrown to the caller must not also reach the listener")
        assertCallerContextIntact()
    }

    @Test
    fun `test a listener that throws is notified once and its exception reaches the caller`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        assignSubject()
        delegate.behaviour = { it.onResponse(TestResponse()) }

        val listenerBug = IllegalStateException("listener bug")
        val calls = AtomicInteger()
        val throwing = object : ActionListener<TestResponse> {
            override fun onResponse(response: TestResponse) {
                calls.incrementAndGet()
                throw listenerBug
            }

            override fun onFailure(e: Exception) {
                calls.incrementAndGet()
            }
        }

        val surfaced = assertThrows<IllegalStateException> { pluginClient.execute(TEST_ACTION, TestRequest(), throwing) }
        assertSame(listenerBug, surfaced)
        assertEquals(1, calls.get(), "the listener must be told exactly once")
        assertCallerContextIntact()
    }

    @Test
    fun `test executing without an assigned subject fails`() {
        val calls = AtomicInteger()
        val e = assertThrows<IllegalStateException> {
            pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({ calls.incrementAndGet() }, { calls.incrementAndGet() }))
        }
        assertEquals("PluginClient is not initialized with a subject.", e.message)
        assertEquals(0, calls.get(), "the listener must not be told about a wiring error")
        assertNull(delegate.capturedListener, "the action must not reach the delegate without a subject")
    }

    @Test
    fun `test the same subject can be assigned twice but a different one is rejected`() {
        val first = assignSubject()
        pluginClient.setSubject(first)

        assertThrows<IllegalStateException> { pluginClient.setSubject(RecordingSubject(threadContext, "set-by-other-subject")) }

        pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap({}, {}))
        assertEquals("set-by-subject", delegate.subjectSeen, "a rejected assignment must leave the first subject in effect")
    }

    @Test
    fun `test a remote cluster client is refused`() {
        assertThrows<UnsupportedOperationException> { pluginClient.getRemoteClusterClient("remote") }
    }

    private fun assertListenerSeesCallerContext(completion: (ActionListener<TestResponse>) -> Unit) {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        assignSubject()

        val callerSeen = AtomicReference<String?>(NOT_RUN)
        val subjectSeen = AtomicReference<String?>(NOT_RUN)
        val record = { _: Any ->
            callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT))
            subjectSeen.set(threadContext.getTransient(SUBJECT_TRANSIENT))
        }
        pluginClient.execute(TEST_ACTION, TestRequest(), ActionListener.wrap(record, record))

        // A response arrives on a pooled thread that never carried the caller's context, so the wrapper around the
        // listener is the only thing that can put it back.
        completeOnAnotherThread(completion)

        assertEquals("set-by-caller", callerSeen.get())
        assertNull(subjectSeen.get(), "the subject's context must not leak into the listener")
    }

    private fun assertCallerContextIntact() {
        assertEquals("set-by-caller", threadContext.getTransient<String>(CALLER_TRANSIENT), "the caller's context must survive")
        assertNull(threadContext.getTransient<String>(SUBJECT_TRANSIENT), "the subject's context must not be left behind")
    }

    private fun assignSubject(): RecordingSubject {
        val subject = RecordingSubject(threadContext)
        pluginClient.setSubject(subject)
        return subject
    }

    /** Completes the captured action on a thread that never carried the caller's context. */
    private fun completeOnAnotherThread(completion: (ActionListener<TestResponse>) -> Unit) {
        val captured = requireNotNull(delegate.capturedListener) { "the action never reached the delegate" }
        responder.submit {
            threadContext.putTransient(RESPONDER_TRANSIENT, "set-by-responder")
            completion(captured)
        }.get(10, TimeUnit.SECONDS)
    }

    /**
     * Stands in for the plugin subject. Stashes and restores around its body as NoopPluginSubject and
     * SecurePluginSubject do, and marks the context so a test can tell the subject's apart from the caller's.
     */
    private class RecordingSubject(
        private val threadContext: ThreadContext,
        private val marker: String = "set-by-subject"
    ) : PluginSubject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin:notifications")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            threadContext.stashContext().use {
                threadContext.putTransient(SUBJECT_TRANSIENT, marker)
                runnable.run()
            }
        }
    }

    /**
     * Records the context the action ran in and keeps the listener, so each test decides when and how the action
     * completes. By default it does not complete, as for an asynchronous action.
     */
    private class CapturingClient(threadPool: ThreadPool) : NoOpClient(threadPool) {
        var capturedListener: ActionListener<TestResponse>? = null
        var subjectSeen: String? = null
        var callerSeen: String? = null
        var behaviour: (ActionListener<TestResponse>) -> Unit = {}

        @Suppress("UNCHECKED_CAST")
        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            subjectSeen = threadPool().threadContext.getTransient(SUBJECT_TRANSIENT)
            callerSeen = threadPool().threadContext.getTransient(CALLER_TRANSIENT)
            val captured = listener as ActionListener<TestResponse>
            capturedListener = captured
            behaviour(captured)
        }

        // Returns a client, as a node client does, so only PluginClient's own override can make the call fail.
        override fun getRemoteClusterClient(clusterAlias: String): Client = this
    }

    private class TestRequest : ActionRequest() {
        override fun validate(): ActionRequestValidationException? = null
    }

    private class TestResponse : ActionResponse() {
        override fun writeTo(out: StreamOutput) {
            // nothing in these tests crosses the wire
        }
    }
}
