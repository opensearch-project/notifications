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
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.utils.recreateObject
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Update NotificationConfig transport action
 */
internal class UpdateNotificationConfigAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<UpdateNotificationConfigRequest, UpdateNotificationConfigResponse>(
    NotificationsActions.UPDATE_NOTIFICATION_CONFIG_NAME,
    transportService,
    client,
    actionFilters,
    ::UpdateNotificationConfigRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        val transformedRequest = request as? UpdateNotificationConfigRequest
            ?: recreateObject(request) { UpdateNotificationConfigRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun executeRequest(
        request: UpdateNotificationConfigRequest,
        user: User?
    ): UpdateNotificationConfigResponse {
        return ConfigIndexingActions.update(request, user)
    }
}
