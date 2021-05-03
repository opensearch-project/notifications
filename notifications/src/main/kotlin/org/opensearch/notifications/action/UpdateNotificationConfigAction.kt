/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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

package org.opensearch.notifications.action

import com.amazon.opendistroforelasticsearch.commons.authuser.User
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.utils.recreateObject
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
        return NotificationConfigActions.update(request, user)
    }
}
