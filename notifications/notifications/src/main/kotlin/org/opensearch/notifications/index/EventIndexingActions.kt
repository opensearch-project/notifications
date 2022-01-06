/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.index

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.action.GetNotificationEventResponse
import org.opensearch.commons.notifications.model.NotificationEventInfo
import org.opensearch.commons.notifications.model.NotificationEventSearchResult
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.security.UserAccess
import org.opensearch.rest.RestStatus

/**
 * NotificationEvent indexing operation actions.
 */
object EventIndexingActions {
    private val log by logger(EventIndexingActions::class.java)

    private lateinit var operations: EventOperations
    private lateinit var userAccess: UserAccess

    fun initialize(operations: EventOperations, userAccess: UserAccess) {
        this.operations = operations
        this.userAccess = userAccess
    }

    /**
     * Get NotificationEvent info
     * @param request [GetNotificationEventRequest] object
     * @param user the user info object
     * @return [GetNotificationEventResponse]
     */
    fun get(request: GetNotificationEventRequest, user: User?): GetNotificationEventResponse {
        log.info("$LOG_PREFIX:NotificationEvent-get $request")
        userAccess.validateUser(user)
        return when (request.eventIds.size) {
            0 -> getAll(request, user)
            1 -> info(request.eventIds.first(), user)
            else -> info(request.eventIds, user)
        }
    }

    /**
     * Get NotificationEvent info
     * @param eventId event id
     * @param user the user info object
     * @return [GetNotificationEventResponse]
     */
    private fun info(eventId: String, user: User?): GetNotificationEventResponse {
        log.info("$LOG_PREFIX:NotificationEvent-info $eventId")
        val eventDoc = operations.getNotificationEvent(eventId)
        eventDoc
            ?: run {
                Metrics.NOTIFICATIONS_EVENTS_INFO_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                throw OpenSearchStatusException("NotificationEvent $eventId not found", RestStatus.NOT_FOUND)
            }
        val metadata = eventDoc.eventDoc.metadata
        if (!userAccess.doesUserHasAccess(user, metadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for NotificationEvent $eventId", RestStatus.FORBIDDEN)
        }
        val eventInfo = NotificationEventInfo(
            eventId,
            metadata.lastUpdateTime,
            metadata.createdTime,
            eventDoc.eventDoc.event
        )
        return GetNotificationEventResponse(NotificationEventSearchResult(eventInfo))
    }

    /**
     * Get NotificationEvent info
     * @param eventIds event id set
     * @param user the user info object
     * @return [GetNotificationEventResponse]
     */
    private fun info(eventIds: Set<String>, user: User?): GetNotificationEventResponse {
        log.info("$LOG_PREFIX:NotificationEvent-info $eventIds")
        val eventDocs = operations.getNotificationEvents(eventIds)
        if (eventDocs.size != eventIds.size) {
            val mutableSet = eventIds.toMutableSet()
            eventDocs.forEach { mutableSet.remove(it.docInfo.id) }
            Metrics.NOTIFICATIONS_EVENTS_INFO_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "NotificationEvent $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        eventDocs.forEach {
            val currentMetadata = it.eventDoc.metadata
            if (!userAccess.doesUserHasAccess(user, currentMetadata.access)) {
                Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "Permission denied for NotificationEvent ${it.docInfo.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val eventSearchResult = eventDocs.map {
            NotificationEventInfo(
                it.docInfo.id!!,
                it.eventDoc.metadata.lastUpdateTime,
                it.eventDoc.metadata.createdTime,
                it.eventDoc.event
            )
        }
        return GetNotificationEventResponse(NotificationEventSearchResult(eventSearchResult))
    }

    /**
     * Get all NotificationEvent matching the criteria
     * @param request [GetNotificationEventRequest] object
     * @param user the user info object
     * @return [GetNotificationEventResponse]
     */
    private fun getAll(request: GetNotificationEventRequest, user: User?): GetNotificationEventResponse {
        log.info("$LOG_PREFIX:NotificationEvent-getAll")
        val searchResult = operations.getAllNotificationEvents(
            userAccess.getSearchAccessInfo(user),
            request
        )
        return GetNotificationEventResponse(searchResult)
    }
}
