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
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.commons.notifications.action.GetChannelListResponse
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import java.lang.Exception

/**
 * Get channel list transport action
 */
internal class GetChannelListAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetChannelListRequest, GetChannelListResponse>(
    NotificationsActions.GET_CHANNEL_LIST_NAME,
    transportService,
    client,
    actionFilters,
    ::GetChannelListRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: ActionRequest,
        listener: ActionListener<GetChannelListResponse>
    ) {
        val transformedRequest = request as? GetChannelListRequest
            ?: recreateObject(request) { GetChannelListRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: GetChannelListRequest,
        user: User?,
        actionListener: ActionListener<GetChannelListResponse>
    ) {
        ConfigIndexingActions.getChannelList(
            request, user,
            object : ActionListener<GetChannelListResponse> {
                override fun onResponse(response: GetChannelListResponse) {
                    actionListener.onResponse(response)
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }
}
