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
package org.opensearch.notifications.index

import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.model.NotificationEventSearchResult
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.model.NotificationEventDocInfo
import org.opensearch.rest.RestStatus

/**
 * Interface for notification events Operations.
 */
interface EventOperations {
    /**
     * create a new doc for NotificationEventDoc
     * @param eventDoc the Notification Event Doc
     * @param id optional id to use as id for the document
     * @return Notification Event id if successful, null otherwise
     * @throws java.util.concurrent.ExecutionException with a cause
     */
    fun createNotificationEvent(eventDoc: NotificationEventDoc, id: String? = null): String?

    /**
     * Query index for Notification Event with ID
     * @param ids set of the document ids to get info
     * @return list of NotificationEventDocInfo on success, null otherwise
     */
    fun getNotificationEvents(ids: Set<String>): List<NotificationEventDocInfo>

    /**
     * Query index for Notification Event with ID
     * @param id the id for the document
     * @return NotificationEventDocInfo on success, null otherwise
     */
    fun getNotificationEvent(id: String): NotificationEventDocInfo?

    /**
     * Query index for NotificationEventDocs for given access details
     * @param access the list of access details to search NotificationEventDocs for.
     * @param request [GetNotificationEventRequest] object
     * @return search result of NotificationEventDocs
     */
    fun getAllNotificationEvents(
        access: List<String>,
        request: GetNotificationEventRequest
    ): NotificationEventSearchResult

    /**
     * update NotificationEventDoc for given id
     * @param id the id for the document
     * @param notificationEventDoc the NotificationEventDoc data
     * @return true if successful, false otherwise
     */
    fun updateNotificationEvent(id: String, notificationEventDoc: NotificationEventDoc): Boolean

    /**
     * delete NotificationEventDoc for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteNotificationEvent(id: String): Boolean

    /**
     * delete NotificationEventDoc for given ids
     * @param ids set of the document ids to delete
     * @return map of id to status
     */
    fun deleteNotificationEvents(ids: Set<String>): Map<String, RestStatus>
}
