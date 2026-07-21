/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.util

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.opensearch.core.action.ActionListener
import org.opensearch.transport.client.OpenSearchClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SuspendUtils {
    companion object {
        suspend fun <C : OpenSearchClient, T> C.suspendUntil(block: C.(ActionListener<T>) -> Unit): T =
            suspendCancellableCoroutine { cont ->
                block(object : ActionListener<T> {
                    override fun onResponse(response: T) = cont.resume(response)

                    override fun onFailure(e: Exception) = cont.resumeWithException(e)
                })
            }

        suspend fun <C : OpenSearchClient, T> C.suspendUntilTimeout(
            timeout: Long,
            block: C.(ActionListener<T>) -> Unit
        ): T {
            var finalValue: T
            withTimeout(timeout) {
                finalValue = suspendUntil(block)
            }
            return finalValue
        }
    }
}
