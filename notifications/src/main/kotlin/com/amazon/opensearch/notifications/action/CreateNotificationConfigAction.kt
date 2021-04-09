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
import com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigResponse
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
 * Create reportDefinition transport action
 */
internal class CreateNotificationConfigAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest, CreateNotificationConfigResponse>(
    NotificationsActions.CREATE_NOTIFICATION_CONFIG_NAME,
    transportService,
    client,
    actionFilters,
    ::CreateNotificationConfigRequest
) {

    /**
     * {@inheritDoc}
     * Transform the request and call super.doExecute() to support call from other plugins.
     */
    override fun doExecute(
        task: Task?,
        request: com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest,
        listener: ActionListener<CreateNotificationConfigResponse>
    ) {
        val transformedRequest = request as? com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest
            ?: recreateObject(request) {
                com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest(
                    it
                )
            }
        super.doExecute(task, transformedRequest, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: com.amazon.opensearch.commons.notifications.action.CreateNotificationConfigRequest,
        user: User?
    ): CreateNotificationConfigResponse {
        return CreateNotificationConfigResponse("TODO-configId")
    }
}
