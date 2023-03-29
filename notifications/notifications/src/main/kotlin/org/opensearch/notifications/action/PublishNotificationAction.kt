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
import org.opensearch.commons.notifications.action.LegacyPublishNotificationRequest
import org.opensearch.commons.notifications.action.LegacyPublishNotificationResponse
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Publish Notification transport action
 *
 * This action is intended only for Index Management use case to support
 * the legacy embedded destinations that are on its policies. No other plugin
 * should utilize this action.
 */
internal class PublishNotificationAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<LegacyPublishNotificationRequest, LegacyPublishNotificationResponse>(
    NotificationsActions.LEGACY_PUBLISH_NOTIFICATION_NAME,
    transportService,
    client,
    actionFilters,
    ::LegacyPublishNotificationRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<LegacyPublishNotificationResponse>
    ) {
        val transformedRequest = request as? LegacyPublishNotificationRequest
            ?: recreateObject(request) { LegacyPublishNotificationRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override suspend fun executeRequest(
        request: LegacyPublishNotificationRequest,
        user: User?
    ): LegacyPublishNotificationResponse {
        return SendMessageActionHelper.executeLegacyRequest(request)
    }
}
