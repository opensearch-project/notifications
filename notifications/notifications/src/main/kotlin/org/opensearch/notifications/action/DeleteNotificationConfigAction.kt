/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.recreateObject
import org.opensearch.core.action.ActionListener
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Delete NotificationConfig transport action
 */
internal class DeleteNotificationConfigAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<DeleteNotificationConfigRequest, DeleteNotificationConfigResponse>(
    NotificationsActions.DELETE_NOTIFICATION_CONFIG_NAME,
    transportService,
    client,
    actionFilters,
    ::DeleteNotificationConfigRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        val transformedRequest = request as? DeleteNotificationConfigRequest
            ?: recreateObject(request) { DeleteNotificationConfigRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun executeRequest(
        request: DeleteNotificationConfigRequest,
        user: User?
    ): DeleteNotificationConfigResponse {
        return ConfigIndexingActions.delete(request, user)
    }
}
