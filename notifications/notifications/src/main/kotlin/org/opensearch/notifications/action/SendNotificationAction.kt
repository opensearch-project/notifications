/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilters
import org.opensearch.common.inject.Inject
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.utils.recreateObject
import org.opensearch.core.action.ActionListener
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.security.UserAccessManager
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client

/**
 * Send Notification transport action
 */
internal class SendNotificationAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<SendNotificationRequest, SendNotificationResponse>(
    NotificationsActions.SEND_NOTIFICATION_NAME,
    transportService,
    client,
    actionFilters,
    ::SendNotificationRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<SendNotificationResponse>
    ) {
        val transformedRequest = request as? SendNotificationRequest
            ?: recreateObject(request) { SendNotificationRequest(it) }
        // A request executed in the caller's own security context is authorized as that caller. In-process callers
        // execute privileged, with no caller context, and carry the user they act for in the request.
        val callerUserInfo: String? =
            client.threadPool().threadContext.getTransient<String>(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val authorizedRequest = if (callerUserInfo != null) {
            SendNotificationRequest(
                transformedRequest.eventSource,
                transformedRequest.channelMessage,
                transformedRequest.channelIds,
                callerUserInfo
            )
        } else {
            transformedRequest
        }
        super.doExecute(task, authorizedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun executeRequest(
        request: SendNotificationRequest,
        user: User?
    ): SendNotificationResponse {
        // The request lists several channels, so the transport-level resource evaluator does not check it; a caller
        // acting in its own security context is checked per channel here.
        if (user != null) {
            request.channelIds.toSet().forEach {
                UserAccessManager.verifyResourceAccess(it, NotificationsActions.SEND_NOTIFICATION_NAME)
            }
        }
        return SendMessageActionHelper.executeRequest(request)
    }
}
