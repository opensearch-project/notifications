/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.common.CheckedRunnable
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.PluginSubject
import org.opensearch.test.client.NoOpClient
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import java.security.Principal
import java.util.concurrent.TimeUnit

internal class PluginClientTests {

    companion object {
        private const val CALLER_HEADER = "x-caller-header"

        private val TEST_ACTION: ActionType<TestResponse> =
            ActionType("cluster:admin/notifications/plugin_client_test", Writeable.Reader { TestResponse() })
    }

    private lateinit var threadPool: ThreadPool
    private lateinit var delegate: CapturingClient
    private lateinit var pluginClient: PluginClient

    @BeforeEach
    fun setup() {
        threadPool = TestThreadPool(PluginClientTests::class.java.simpleName)
        delegate = CapturingClient(threadPool)
        pluginClient = PluginClient(delegate)
    }

    @AfterEach
    fun teardown() {
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `test caller context is restored before a successful listener runs`() {
        pluginClient.setSubject(selfRestoringSubject())
        threadPool.threadContext.putHeader(CALLER_HEADER, "caller-value")

        var headerSeenByListener: String? = null
        pluginClient.execute(
            TEST_ACTION,
            TestRequest(),
            object : ActionListener<TestResponse> {
                override fun onResponse(response: TestResponse) {
                    headerSeenByListener = threadPool.threadContext.getHeader(CALLER_HEADER)
                }

                override fun onFailure(e: Exception) = fail("unexpected failure", e)
            }
        )

        val captured = requireNotNull(delegate.capturedListener) { "the action never reached the delegate" }
        onAThreadWithoutTheCallerContext { captured.onResponse(TestResponse()) }

        assertEquals("caller-value", headerSeenByListener)
    }

    @Test
    fun `test caller context is restored before a failing listener runs`() {
        pluginClient.setSubject(selfRestoringSubject())
        threadPool.threadContext.putHeader(CALLER_HEADER, "caller-value")

        var headerSeenByListener: String? = null
        pluginClient.execute(
            TEST_ACTION,
            TestRequest(),
            object : ActionListener<TestResponse> {
                override fun onResponse(response: TestResponse) = fail("unexpected response")

                override fun onFailure(e: Exception) {
                    headerSeenByListener = threadPool.threadContext.getHeader(CALLER_HEADER)
                }
            }
        )

        val captured = requireNotNull(delegate.capturedListener) { "the action never reached the delegate" }
        onAThreadWithoutTheCallerContext { captured.onFailure(IllegalStateException("transport failure")) }

        assertEquals("caller-value", headerSeenByListener)
    }

    @Test
    fun `test a synchronous failure is reported through the listener`() {
        pluginClient.setSubject(selfRestoringSubject())
        delegate.failWith = IllegalArgumentException("synchronous failure")

        var reported: Exception? = null
        pluginClient.execute(
            TEST_ACTION,
            TestRequest(),
            object : ActionListener<TestResponse> {
                override fun onResponse(response: TestResponse) = fail("unexpected response")

                override fun onFailure(e: Exception) {
                    reported = e
                }
            }
        )

        assertTrue(reported is IllegalArgumentException, "expected the delegate failure, got $reported")
        assertEquals("synchronous failure", reported?.message)
    }

    @Test
    fun `test executing without an assigned subject fails`() {
        assertThrows<IllegalStateException> {
            pluginClient.execute(
                TEST_ACTION,
                TestRequest(),
                object : ActionListener<TestResponse> {
                    override fun onResponse(response: TestResponse) = fail("unexpected response")

                    override fun onFailure(e: Exception) = fail("unexpected failure", e)
                }
            )
        }
        assertNull(delegate.capturedListener, "the action must not reach the delegate without a subject")
    }

    /**
     * Both real subject implementations wrap the body of runAs in a stash that restores on exit, so a fake that does
     * not would let a client pass here while behaving differently against a cluster.
     */
    private fun selfRestoringSubject(): PluginSubject = object : PluginSubject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin:notifications")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            val stashed = threadPool.threadContext.stashContext()
            try {
                runnable.run()
            } finally {
                stashed.close()
            }
        }
    }

    /**
     * A transport response is handed back on a pooled thread that never carried the caller's context, which is the
     * only situation in which the restore wired to the listener is observable.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun onAThreadWithoutTheCallerContext(block: () -> Unit) {
        var failure: Throwable? = null
        val thread = Thread {
            try {
                assertNull(threadPool.threadContext.getHeader(CALLER_HEADER))
                block()
            } catch (t: Throwable) {
                failure = t
            }
        }
        thread.start()
        thread.join(TimeUnit.SECONDS.toMillis(10))
        failure?.let { throw it }
    }

    private class CapturingClient(threadPool: ThreadPool) : NoOpClient(threadPool) {
        var capturedListener: ActionListener<TestResponse>? = null
        var failWith: Exception? = null

        @Suppress("UNCHECKED_CAST")
        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            failWith?.let { throw it }
            capturedListener = listener as ActionListener<TestResponse>
        }
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
