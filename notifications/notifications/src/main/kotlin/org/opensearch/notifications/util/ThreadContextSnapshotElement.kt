/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import kotlinx.coroutines.ThreadContextElement
import org.opensearch.common.util.concurrent.ThreadContext
import kotlin.coroutines.CoroutineContext

/**
 * Carries a captured [ThreadContext] across a coroutine's thread hops.
 *
 * Each time the coroutine starts or resumes on a thread, [snapshot] is installed there; when it suspends or
 * completes, that thread's own context is put back. Code after a suspension point therefore still sees the caller's
 * security context, and no pooled thread is left holding it.
 */
internal class ThreadContextSnapshotElement(
    private val threadContext: ThreadContext,
    private val snapshot: ThreadContext.StoredContext
) : ThreadContextElement<ThreadContext.StoredContext> {
    companion object Key : CoroutineContext.Key<ThreadContextSnapshotElement>

    override val key: CoroutineContext.Key<ThreadContextSnapshotElement> = Key

    override fun updateThreadContext(context: CoroutineContext): ThreadContext.StoredContext {
        val previous = threadContext.newStoredContext(false)
        snapshot.restore()
        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: ThreadContext.StoredContext) {
        oldState.restore()
    }
}
