/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/*
package org.opensearch.notifications.action

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.action.GetNotificationEventResponse
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.index.EventIndexingActions
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Get notification event transport action
 */
internal class GetNotificationEventAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetNotificationEventRequest, GetNotificationEventResponse>(
    NotificationsActions.GET_NOTIFICATION_EVENT_NAME,
    transportService,
    client,
    actionFilters,
    ::GetNotificationEventRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<GetNotificationEventResponse>
    ) {
        val transformedRequest = request as? GetNotificationEventRequest
            ?: recreateObject(request) { GetNotificationEventRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: GetNotificationEventRequest,
        user: User?
    ): GetNotificationEventResponse {
        return EventIndexingActions.get(request, user)
    }
}

 */
