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

package com.amazon.opendistroforelasticsearch.notifications.action

import com.amazon.opendistroforelasticsearch.commons.authuser.User
import com.amazon.opendistroforelasticsearch.commons.notifications.action.NotificationsActions
import com.amazon.opendistroforelasticsearch.commons.notifications.action.UpdateNotificationConfigRequest
import com.amazon.opendistroforelasticsearch.commons.notifications.action.UpdateNotificationConfigResponse
import com.amazon.opendistroforelasticsearch.notifications.util.recreateObject
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.support.ActionFilters
import org.elasticsearch.client.Client
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.tasks.Task
import org.elasticsearch.transport.TransportService

/**
 * Update reportDefinition transport action
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
        request: UpdateNotificationConfigRequest,
        listener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        val transformedRequest = request as? UpdateNotificationConfigRequest
            ?: recreateObject(request) { UpdateNotificationConfigRequest(it) }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: UpdateNotificationConfigRequest,
        user: User?
    ): UpdateNotificationConfigResponse {
        return UpdateNotificationConfigResponse("TODO-configId")
    }
}
