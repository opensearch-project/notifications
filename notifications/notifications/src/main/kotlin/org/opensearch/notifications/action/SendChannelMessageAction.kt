/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.common.inject.Inject
import org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.notifications.model.SendChannelMessageRequest
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client

/**
 * Sends a caller-supplied message to one existing channel, authorized as the caller.
 *
 * This is the action behind the REST send route. In-process callers such as Alerting keep using the send
 * notification action, which this action does not change.
 */
internal class SendChannelMessageAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters
) : PluginBaseAction<SendChannelMessageRequest, SendNotificationResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::SendChannelMessageRequest
) {
    companion object {
        internal const val NAME = "cluster:admin/opensearch/notifications/channels/send"
        internal val ACTION_TYPE = ActionType(NAME, ::SendNotificationResponse)
    }

    /**
     * {@inheritDoc}
     * The channel itself is checked by the security plugin because the request names one channel. An email channel
     * also references an account and recipient groups, which are separate resources, so those are verified here.
     */
    override suspend fun executeRequest(
        request: SendChannelMessageRequest,
        user: User?
    ): SendNotificationResponse {
        // The user that delivery authorizes against is always the authenticated caller, never the request.
        val callerUserInfo: String? =
            client.threadPool().threadContext.getTransient<String>(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
        val sendRequest = SendNotificationRequest(
            request.eventSource,
            request.channelMessage,
            listOf(request.configId),
            callerUserInfo
        )
        return SendMessageActionHelper.executeRequest(sendRequest, childConfigAccessAction = NAME)
    }
}
