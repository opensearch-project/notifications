/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opensearch.OpenSearchSecurityException
import org.opensearch.OpenSearchStatusException
import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilters
import org.opensearch.action.support.HandledTransportAction
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.authuser.User
import org.opensearch.commons.utils.logger
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.rest.RestStatus
import org.opensearch.index.IndexNotFoundException
import org.opensearch.index.engine.VersionConflictEngineException
import org.opensearch.indices.InvalidIndexNameException
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client
import java.io.IOException

internal abstract class PluginBaseAction<Request : ActionRequest, Response : ActionResponse>(
    name: String,
    transportService: TransportService,
    val client: Client,
    actionFilters: ActionFilters,
    requestReader: Writeable.Reader<ActionRequest>
) : HandledTransportAction<ActionRequest, Response>(name, transportService, actionFilters, requestReader) {
    companion object {
        private val log by logger(PluginBaseAction::class.java)
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    }

    /**
     * {@inheritDoc}
     */
    @Suppress("TooGenericExceptionCaught", "UNCHECKED_CAST")
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<Response>
    ) {
        val userStr: String? =
            client.threadPool().threadContext.getTransient<String>(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val user: User? = User.parse(userStr)
        val storedThreadContext = client.threadPool().threadContext.newStoredContext(false)
        scope.launch {
            try {
                client.threadPool().threadContext.stashContext().use {
                    storedThreadContext.restore()
                    listener.onResponse(executeRequest(request as Request, user))
                }
            } catch (exception: OpenSearchStatusException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_OS_STATUS_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:OpenSearchStatusException:", exception)
                listener.onFailure(exception)
            } catch (exception: OpenSearchSecurityException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_OS_SECURITY_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:OpenSearchSecurityException:", exception)
                listener.onFailure(
                    OpenSearchStatusException(
                        "Permissions denied: ${exception.message} - Contact administrator",
                        RestStatus.FORBIDDEN
                    )
                )
            } catch (exception: VersionConflictEngineException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_VERSION_CONFLICT_ENGINE_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:VersionConflictEngineException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.CONFLICT))
            } catch (exception: IndexNotFoundException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_INDEX_NOT_FOUND_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:IndexNotFoundException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.NOT_FOUND))
            } catch (exception: InvalidIndexNameException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_INVALID_INDEX_NAME_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:InvalidIndexNameException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.BAD_REQUEST))
            } catch (exception: IllegalArgumentException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_ILLEGAL_ARGUMENT_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:IllegalArgumentException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.BAD_REQUEST))
            } catch (exception: IllegalStateException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_ILLEGAL_STATE_EXCEPTION.counter.increment()
                log.warn("$LOG_PREFIX:IllegalStateException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.SERVICE_UNAVAILABLE))
            } catch (exception: IOException) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_IO_EXCEPTION.counter.increment()
                log.error("$LOG_PREFIX:Uncaught IOException:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.FAILED_DEPENDENCY))
            } catch (exception: Exception) {
                Metrics.NOTIFICATIONS_EXCEPTIONS_INTERNAL_SERVER_ERROR.counter.increment()
                log.error("$LOG_PREFIX:Uncaught Exception:", exception)
                listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.INTERNAL_SERVER_ERROR))
            }
        }
    }

    /**
     * Execute the transport request
     * @param request the request to execute
     * @param user the user context given by security plugin
     * @return the response to return.
     */
    abstract suspend fun executeRequest(request: Request, user: User?): Response

    /**
     * Executes the given [block] function on this resource and then closes it down correctly whether an exception
     * is thrown or not.
     *
     * In case if the resource is being closed due to an exception occurred in [block], and the closing also fails with an exception,
     * the latter is added to the [suppressed][java.lang.Throwable.addSuppressed] exceptions of the former.
     *
     * @param block a function to process this [AutoCloseable] resource.
     * @return the result of [block] function invoked on this resource.
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun <T : ThreadContext.StoredContext, R> T.use(block: (T) -> R): R {
        var exception: Throwable? = null
        try {
            return block(this)
        } catch (e: Throwable) {
            exception = e
            throw e
        } finally {
            closeFinally(exception)
        }
    }

    /**
     * Closes this [AutoCloseable], suppressing possible exception or error thrown by [AutoCloseable.close] function when
     * it's being closed due to some other [cause] exception occurred.
     *
     * The suppressed exception is added to the list of suppressed exceptions of [cause] exception.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun ThreadContext.StoredContext.closeFinally(cause: Throwable?) = when (cause) {
        null -> close()
        else -> try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
    }
}
