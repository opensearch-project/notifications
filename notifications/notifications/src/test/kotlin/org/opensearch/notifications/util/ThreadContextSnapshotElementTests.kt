/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.util

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opensearch.common.settings.Settings
import org.opensearch.common.util.concurrent.ThreadContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class ThreadContextSnapshotElementTests {
    private val header = "caller"
    private lateinit var threadContext: ThreadContext
    private lateinit var first: ExecutorService
    private lateinit var second: ExecutorService

    @BeforeEach
    fun setup() {
        threadContext = ThreadContext(Settings.EMPTY)
        first = Executors.newSingleThreadExecutor()
        second = Executors.newSingleThreadExecutor()
    }

    @AfterEach
    fun cleanup() {
        first.shutdownNow()
        second.shutdownNow()
    }

    /** The caller's context, captured the way PluginBaseAction captures it from the transport thread. */
    private fun callerSnapshot(): ThreadContextSnapshotElement {
        threadContext.stashContext().use {
            threadContext.putHeader(header, "alice")
            return ThreadContextSnapshotElement(threadContext, threadContext.newStoredContext(false))
        }
    }

    private fun headerOn(executor: ExecutorService): String? = executor.submit<String?> { threadContext.getHeader(header) }.get()

    @Test
    fun `the caller context is present on every thread the coroutine resumes on`() {
        val element = callerSnapshot()
        val seen = mutableListOf<String?>()

        runBlocking(first.asCoroutineDispatcher() + element) {
            seen += threadContext.getHeader(header)
            withContext(second.asCoroutineDispatcher()) { seen += threadContext.getHeader(header) }
            // Back on the first thread after a suspension
            seen += threadContext.getHeader(header)
        }

        assertEquals(listOf("alice", "alice", "alice"), seen)
    }

    @Test
    fun `no pooled thread keeps the caller context afterwards`() {
        val element = callerSnapshot()

        runBlocking(first.asCoroutineDispatcher() + element) {
            withContext(second.asCoroutineDispatcher()) { threadContext.getHeader(header) }
        }

        assertNull(headerOn(first))
        assertNull(headerOn(second))
        assertNull(threadContext.getHeader(header))
    }

    @Test
    fun `a thread's own context is put back when the coroutine leaves it`() {
        first.submit { threadContext.putHeader(header, "pooled") }.get()
        val element = callerSnapshot()

        runBlocking(first.asCoroutineDispatcher() + element) {
            assertEquals("alice", threadContext.getHeader(header))
        }

        assertEquals("pooled", headerOn(first))
    }
}
