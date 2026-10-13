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
import org.opensearch.notifications.NotificationPlugin.Companion.TENANT_ID_HEADER
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.util.TenantContext
import org.opensearch.notifications.util.ThreadContextSnapshotElement
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
        val tenantId: String? = client.threadPool().threadContext.getHeader(TENANT_ID_HEADER)
        val threadContext = client.threadPool().threadContext
        // The coroutine hops between Dispatchers.IO threads at every suspension, so the caller's context is
        // reinstalled on each thread it runs on instead of only the first one
        val callerContext = ThreadContextSnapshotElement(threadContext, threadContext.newStoredContext(false))
        scope.launch(TenantContext(tenantId) + callerContext) {
            try {
                listener.onResponse(executeRequest(request as Request, user))
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
}
