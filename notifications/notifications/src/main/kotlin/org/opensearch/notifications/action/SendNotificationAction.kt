/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

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
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun executeRequest(
        request: SendNotificationRequest,
        user: User?
    ): SendNotificationResponse {
        return SendMessageActionHelper.executeRequest(request)
    }
}
