/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.index

import org.opensearch.action.ActionListener
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
    fun createNotificationEvent(
        eventDoc: NotificationEventDoc,
        id: String? = null,
        actionListener: ActionListener<String>
    )

    /**
     * Query index for Notification Event with ID
     * @param ids set of the document ids to get info
     * @return list of NotificationEventDocInfo on success, null otherwise
     */
    fun getNotificationEvents(
        ids: Set<String>,
        actionListener: ActionListener<List<NotificationEventDocInfo>>
    )

    /**
     * Query index for Notification Event with ID
     * @param id the id for the document
     * @return NotificationEventDocInfo on success, null otherwise
     */
    fun getNotificationEvent(
        id: String,
        actionListener: ActionListener<NotificationEventDocInfo>
    )

    /**
     * Query index for NotificationEventDocs for given access details
     * @param access the list of access details to search NotificationEventDocs for.
     * @param request [GetNotificationEventRequest] object
     * @return search result of NotificationEventDocs
     */
    fun getAllNotificationEvents(
        access: List<String>,
        request: GetNotificationEventRequest,
        actionListener: ActionListener<NotificationEventSearchResult>
    )

    /**
     * update NotificationEventDoc for given id
     * @param id the id for the document
     * @param notificationEventDoc the NotificationEventDoc data
     * @param actionListener listener which returns success or failure on doc indexed
     * @return true if successful, false otherwise
     */
    fun updateNotificationEvent(
        id: String,
        notificationEventDoc: NotificationEventDoc,
        actionListener: ActionListener<Boolean>
    )

    /**
     * delete NotificationEventDoc for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteNotificationEvent(
        id: String,
        actionListener: ActionListener<Boolean>
    )

    /**
     * delete NotificationEventDoc for given ids
     * @param ids set of the document ids to delete
     * @return map of id to status
     */
    fun deleteNotificationEvents(
        ids: Set<String>,
        actionListener: ActionListener<Map<String, RestStatus>>
    )
}
