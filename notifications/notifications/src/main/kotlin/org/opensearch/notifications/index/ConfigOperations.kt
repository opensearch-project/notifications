/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
package org.opensearch.notifications.index

import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.rest.RestStatus

/**
 * Interface for config Operations.
 */
interface ConfigOperations {
    /**
     * create a new doc for NotificationConfigDoc
     * @param configDoc the Notification Config Doc
     * @param id optional id to use as id for the document
     * @return Notification Config id if successful, null otherwise
     * @throws java.util.concurrent.ExecutionException with a cause
     */
    fun createNotificationConfig(configDoc: NotificationConfigDoc, id: String? = null): String?

    /**
     * Query index for Notification Config with ID
     * @param ids set of the document ids to get info
     * @return list of NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfigs(ids: Set<String>): List<NotificationConfigDocInfo>

    /**
     * Query index for Notification Config with ID
     * @param id the id for the document
     * @return NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfig(id: String): NotificationConfigDocInfo?

    /**
     * Query index for NotificationConfigDocs for given access details
     * @param access the list of access details to search NotificationConfigDocs for.
     * @param request [GetNotificationConfigRequest] object
     * @return search result of NotificationConfigDocs
     */
    fun getAllNotificationConfigs(
        access: List<String>,
        request: GetNotificationConfigRequest
    ): NotificationConfigSearchResult

    /**
     * update NotificationConfigDoc for given id
     * @param id the id for the document
     * @param notificationConfigDoc the NotificationConfigDoc data
     * @return true if successful, false otherwise
     */
    fun updateNotificationConfig(id: String, notificationConfigDoc: NotificationConfigDoc): Boolean

    /**
     * delete NotificationConfigDoc for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteNotificationConfig(id: String): Boolean

    /**
     * delete NotificationConfigDoc for given ids
     * @param ids set of the document ids to delete
     * @return map of id to status
     */
    fun deleteNotificationConfigs(ids: Set<String>): Map<String, RestStatus>
}
