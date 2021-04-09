/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opensearch.notifications.action

import com.amazon.opendistroforelasticsearch.commons.authuser.User
import com.amazon.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import com.amazon.opensearch.commons.notifications.action.NotificationsActions
import com.amazon.opensearch.commons.utils.recreateObject
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Delete reportDefinition transport action
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
        request: DeleteNotificationConfigRequest,
        listener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        val transformedRequest = request as? DeleteNotificationConfigRequest
            ?: recreateObject(request) { DeleteNotificationConfigRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: DeleteNotificationConfigRequest,
        user: User?
    ): DeleteNotificationConfigResponse {
        return DeleteNotificationConfigResponse("TODO-configId")
    }
}
