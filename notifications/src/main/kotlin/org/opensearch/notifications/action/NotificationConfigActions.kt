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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import org.opensearch.OpenSearchStatusException
import org.opensearch.common.Strings
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.security.UserAccessManager
import org.opensearch.rest.RestStatus
import java.time.Instant

/**
 * NotificationConfig index operation actions.
 */
internal object NotificationConfigActions {
    private val log by logger(NotificationConfigActions::class.java)

    /**
     * Create new NotificationConfig
     * @param request [CreateNotificationConfigRequest] object
     * @param user the user info object
     * @return [CreateNotificationConfigResponse]
     */
    fun create(request: CreateNotificationConfigRequest, user: User?): CreateNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val metadata = DocMetadata(
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user)
        )
        val configDoc = NotificationConfigDoc(metadata, request.notificationConfig)
        val docId = NotificationConfigIndex.createNotificationConfig(configDoc, request.configId)
        docId ?: throw OpenSearchStatusException(
            "NotificationConfig Creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        return CreateNotificationConfigResponse(docId)
    }

    /**
     * Update NotificationConfig
     * @param request [UpdateNotificationConfigRequest] object
     * @param user the user info object
     * @return [UpdateNotificationConfigResponse]
     */
    fun update(request: UpdateNotificationConfigRequest, user: User?): UpdateNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-update ${request.configId}")
        UserAccessManager.validateUser(user)
        val currentConfigDoc = NotificationConfigIndex.getNotificationConfig(request.configId)
        currentConfigDoc
            ?: run {
                throw OpenSearchStatusException(
                    "NotificationConfig ${request.configId} not found",
                    RestStatus.NOT_FOUND
                )
            }

        val currentMetadata = currentConfigDoc.configDoc.metadata
        if (!UserAccessManager.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
            throw OpenSearchStatusException(
                "Permission denied for NotificationConfig ${request.configId}",
                RestStatus.FORBIDDEN
            )
        }
        val newMetadata = currentMetadata.copy(lastUpdateTime = Instant.now())
        val newConfigData = NotificationConfigDoc(newMetadata, request.notificationConfig)
        if (!NotificationConfigIndex.updateNotificationConfig(request.configId, newConfigData)) {
            throw OpenSearchStatusException("NotificationConfig Update failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        return UpdateNotificationConfigResponse(request.configId)
    }

    /**
     * Get NotificationConfig info
     * @param request [GetNotificationConfigRequest] object
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    fun get(request: GetNotificationConfigRequest, user: User?): GetNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-get $request")
        UserAccessManager.validateUser(user)
        return if (request.configId == null || Strings.isEmpty(request.configId)) {
            getAll(request.fromIndex, request.maxItems, user)
        } else {
            info(request.configId, user)
        }
    }

    /**
     * Get NotificationConfig info
     * @param configId config id
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun info(configId: String, user: User?): GetNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-info $configId")
        val configDoc = NotificationConfigIndex.getNotificationConfig(configId)
        configDoc
            ?: run {
                throw OpenSearchStatusException("NotificationConfig $configId not found", RestStatus.NOT_FOUND)
            }
        val metadata = configDoc.configDoc.metadata
        if (!UserAccessManager.doesUserHasAccess(user, metadata.tenant, metadata.access)) {
            throw OpenSearchStatusException("Permission denied for NotificationConfig $configId", RestStatus.FORBIDDEN)
        }
        val configInfo = NotificationConfigInfo(
            configId,
            metadata.lastUpdateTime,
            metadata.createdTime,
            metadata.tenant,
            configDoc.configDoc.config
        )
        return GetNotificationConfigResponse(NotificationConfigSearchResult(configInfo))
    }

    /**
     * Get all NotificationConfig matching the criteria
     * @param fromIndex the paginated start index
     * @param maxItems the max items to query
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun getAll(fromIndex: Int, maxItems: Int, user: User?): GetNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-getAll fromIndex:$fromIndex maxItems:$maxItems")
        val searchResult = NotificationConfigIndex.getAllNotificationConfigs(
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getSearchAccessInfo(user),
            fromIndex,
            maxItems
        )
        return GetNotificationConfigResponse(searchResult)
    }

    /**
     * Delete NotificationConfig
     * @param configId NotificationConfig object id
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    private fun delete(configId: String, user: User?): DeleteNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-delete $configId")
        UserAccessManager.validateUser(user)
        val currentConfigDoc = NotificationConfigIndex.getNotificationConfig(configId)
        currentConfigDoc
            ?: run {
                throw OpenSearchStatusException(
                    "NotificationConfig $configId not found",
                    RestStatus.NOT_FOUND
                )
            }

        val currentMetadata = currentConfigDoc.configDoc.metadata
        if (!UserAccessManager.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
            throw OpenSearchStatusException(
                "Permission denied for NotificationConfig $configId",
                RestStatus.FORBIDDEN
            )
        }
        if (!NotificationConfigIndex.deleteNotificationConfig(configId)) {
            throw OpenSearchStatusException(
                "NotificationConfig $configId delete failed",
                RestStatus.REQUEST_TIMEOUT
            )
        }
        return DeleteNotificationConfigResponse(mapOf(Pair(configId, RestStatus.OK)))
    }

    /**
     * Delete NotificationConfig
     * @param configIds NotificationConfig object ids
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    private fun delete(configIds: Set<String>, user: User?): DeleteNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-delete $configIds")
        UserAccessManager.validateUser(user)
        val configDocs = NotificationConfigIndex.getNotificationConfigs(configIds)
        if (configDocs.size != configIds.size) {
            val mutableSet = configIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.docInfo.id) }
            throw OpenSearchStatusException(
                "NotificationConfig $configDocs not found",
                RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            val currentMetadata = it.configDoc.metadata
            if (!UserAccessManager.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
                throw OpenSearchStatusException(
                    "Permission denied for NotificationConfig ${it.docInfo.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val deleteStatus = NotificationConfigIndex.deleteNotificationConfigs(configIds)
        return DeleteNotificationConfigResponse(deleteStatus)
    }

    /**
     * Delete NotificationConfig
     * @param request [DeleteNotificationConfigRequest] object
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    fun delete(request: DeleteNotificationConfigRequest, user: User?): DeleteNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-delete ${request.configIds}")
        return if (request.configIds.size == 1) {
            delete(request.configIds.first(), user)
        } else {
            delete(request.configIds, user)
        }
    }
}
