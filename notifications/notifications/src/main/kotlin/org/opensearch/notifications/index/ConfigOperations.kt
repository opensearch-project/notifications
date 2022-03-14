/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.index

import org.opensearch.action.ActionListener
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
    fun createNotificationConfig(
        configDoc: NotificationConfigDoc,
        id: String? = null,
        actionListener: ActionListener<String>
    )

    /**
     * Query index for Notification Config with ID
     * @param ids set of the document ids to get info
     * @return list of NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfigs(
        ids: Set<String>,
        actionListener: ActionListener<List<NotificationConfigDocInfo>>
    )

    /**
     * Query index for Notification Config with ID
     * @param id the id for the document
     * @return NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfig(
        id: String,
        actionListener: ActionListener<NotificationConfigDocInfo>
    )

    /**
     * Query index for NotificationConfigDocs for given access details
     * @param access the list of access details to search NotificationConfigDocs for.
     * @param request [GetNotificationConfigRequest] object
     * @return search result of NotificationConfigDocs
     */
    fun getAllNotificationConfigs(
        access: List<String>,
        request: GetNotificationConfigRequest,
        actionListener: ActionListener<NotificationConfigSearchResult>
    )

    /**
     * update NotificationConfigDoc for given id
     * @param id the id for the document
     * @param notificationConfigDoc the NotificationConfigDoc data
     * @return true if successful, false otherwise
     */
    fun updateNotificationConfig(
        id: String,
        notificationConfigDoc: NotificationConfigDoc,
        actionListener: ActionListener<Boolean>
    )

    /**
     * delete NotificationConfigDoc for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteNotificationConfig(id: String, actionListener: ActionListener<Boolean>)

    /**
     * delete NotificationConfigDoc for given ids
     * @param ids set of the document ids to delete
     * @return map of id to status
     */
    fun deleteNotificationConfigs(ids: Set<String>, actionListener: ActionListener<Map<String, RestStatus>>)
}
