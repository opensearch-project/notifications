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
import org.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import org.opensearch.commons.notifications.action.GetFeatureChannelListResponse
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Get feature channel list transport action
 */
internal class GetFeatureChannelListAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetFeatureChannelListRequest, GetFeatureChannelListResponse>(
    NotificationsActions.GET_FEATURE_CHANNEL_LIST_NAME,
    transportService,
    client,
    actionFilters,
    ::GetFeatureChannelListRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<GetFeatureChannelListResponse>
    ) {
        val transformedRequest = request as? GetFeatureChannelListRequest
            ?: recreateObject(request) { GetFeatureChannelListRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: GetFeatureChannelListRequest,
        user: User?
    ): GetFeatureChannelListResponse {
        return ConfigIndexingActions.getFeatureChannelList(request, user)
    }
}
