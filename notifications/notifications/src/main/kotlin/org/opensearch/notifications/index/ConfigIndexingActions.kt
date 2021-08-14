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

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import org.opensearch.commons.notifications.action.GetFeatureChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.FeatureChannel
import org.opensearch.commons.notifications.model.FeatureChannelList
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.SesAccount
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Sns
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.security.UserAccess
import org.opensearch.rest.RestStatus
import java.time.Instant

/**
 * NotificationConfig indexing operation actions.
 */
@Suppress("TooManyFunctions")
object ConfigIndexingActions {
    private val log by logger(ConfigIndexingActions::class.java)

    private lateinit var operations: ConfigOperations
    private lateinit var userAccess: UserAccess

    fun initialize(operations: ConfigOperations, userAccess: UserAccess) {
        this.operations = operations
        this.userAccess = userAccess
    }

    @Suppress("UnusedPrivateMember")
    private fun validateSlackConfig(slack: Slack, user: User?) {
        // TODO: URL validation with rules
    }

    @Suppress("UnusedPrivateMember")
    private fun validateChimeConfig(chime: Chime, user: User?) {
        // TODO: URL validation with rules
    }

    @Suppress("UnusedPrivateMember")
    private fun validateWebhookConfig(webhook: Webhook, user: User?) {
        // TODO: URL validation with rules
    }

    @Suppress("UnusedPrivateMember")
    private fun validateSnsConfig(sns: Sns, user: User?) {
        // TODO: URL validation with rules
    }

    private fun validateEmailConfig(email: Email, features: Set<String>, user: User?) {
        if (email.emailGroupIds.contains(email.emailAccountID)) {
            throw OpenSearchStatusException(
                "Config IDs ${email.emailAccountID} is in both emailAccountID and emailGroupIds",
                RestStatus.BAD_REQUEST
            )
        }
        val configIds = setOf(email.emailAccountID).union(email.emailGroupIds)
        val configDocs = operations.getNotificationConfigs(configIds)
        if (configDocs.size != configIds.size) {
            val availableIds = configDocs.map { it.docInfo.id }.toSet()
            throw OpenSearchStatusException(
                "Config IDs not found:${configIds.filterNot { availableIds.contains(it) }}",
                RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            // Validate that the config type matches the data
            when (it.configDoc.config.configType) {
                ConfigType.EMAIL_GROUP -> if (it.docInfo.id == email.emailAccountID) {
                    // Email Group ID is specified as Email Account ID
                    Metrics.NOTIFICATIONS_CONFIG_USER_ERROR_INVALID_EMAIL_ACCOUNT_ID.counter.increment()
                    throw OpenSearchStatusException(
                        "configId ${it.docInfo.id} is not a valid email account ID",
                        RestStatus.NOT_ACCEPTABLE
                    )
                }
                ConfigType.SMTP_ACCOUNT -> if (it.docInfo.id != email.emailAccountID) {
                    // Email Account ID is specified as Email Group ID
                    Metrics.NOTIFICATIONS_CONFIG_USER_ERROR_INVALID_EMAIL_GROUP_ID.counter.increment()
                    throw OpenSearchStatusException(
                        "configId ${it.docInfo.id} is not a valid email group ID",
                        RestStatus.NOT_ACCEPTABLE
                    )
                }
                else -> {
                    // Config ID is neither Email Group ID or valid Email Account ID
                    Metrics.NOTIFICATIONS_CONFIG_USER_ERROR_NEITHER_EMAIL_NOR_GROUP.counter.increment()
                    throw OpenSearchStatusException(
                        "configId ${it.docInfo.id} is not a valid email group ID or email account ID",
                        RestStatus.NOT_ACCEPTABLE
                    )
                }
            }
            // Validate that the user has access to underlying configurations as well.
            val currentMetadata = it.configDoc.metadata
            if (!userAccess.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
                Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "Permission denied for NotificationConfig ${it.docInfo.id}",
                    RestStatus.FORBIDDEN
                )
            }

            // Validate the features enabled are included in all underlying configurations as well.
            if (!it.configDoc.config.features.containsAll(features)) {
                val missingFeatures = features.filterNot { item ->
                    it.configDoc.config.features.contains(item)
                }
                Metrics.NOTIFICATIONS_SECURITY_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "Some Features not available in NotificationConfig ${it.docInfo.id}:$missingFeatures",
                    RestStatus.FORBIDDEN
                )
            }
        }
    }

    @Suppress("UnusedPrivateMember")
    private fun validateSmtpAccountConfig(smtpAccount: SmtpAccount, user: User?) {
        // TODO: host validation with rules
    }

    @Suppress("UnusedPrivateMember")
    private fun validateSesAccountConfig(sesAccount: SesAccount, user: User?) {
        // TODO: host validation with rules
    }

    @Suppress("UnusedPrivateMember")
    private fun validateEmailGroupConfig(emailGroup: EmailGroup, user: User?) {
        // No extra validation required. All email IDs are validated as part of model validation.
    }

    private fun validateConfig(config: NotificationConfig, user: User?) {
        when (config.configType) {
            ConfigType.NONE -> throw OpenSearchStatusException(
                "NotificationConfig with type NONE is not acceptable",
                RestStatus.NOT_ACCEPTABLE
            )
            ConfigType.SLACK -> validateSlackConfig(config.configData as Slack, user)
            ConfigType.CHIME -> validateChimeConfig(config.configData as Chime, user)
            ConfigType.WEBHOOK -> validateWebhookConfig(config.configData as Webhook, user)
            ConfigType.EMAIL -> validateEmailConfig(config.configData as Email, config.features, user)
            ConfigType.SMTP_ACCOUNT -> validateSmtpAccountConfig(config.configData as SmtpAccount, user)
            ConfigType.SES_ACCOUNT -> validateSesAccountConfig(config.configData as SesAccount, user)
            ConfigType.EMAIL_GROUP -> validateEmailGroupConfig(config.configData as EmailGroup, user)
            ConfigType.SNS -> validateSnsConfig(config.configData as Sns, user)
        }
    }

    /**
     * Create new NotificationConfig
     * @param request [CreateNotificationConfigRequest] object
     * @param user the user info object
     * @return [CreateNotificationConfigResponse]
     */
    fun create(request: CreateNotificationConfigRequest, user: User?): CreateNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-create")
        userAccess.validateUser(user)
        validateConfig(request.notificationConfig, user)
        val currentTime = Instant.now()
        val metadata = DocMetadata(
            currentTime,
            currentTime,
            userAccess.getUserTenant(user),
            userAccess.getAllAccessInfo(user)
        )
        val configDoc = NotificationConfigDoc(metadata, request.notificationConfig)
        val docId = operations.createNotificationConfig(configDoc, request.configId)
        docId ?: run {
            Metrics.NOTIFICATIONS_CONFIG_CREATE_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "NotificationConfig Creation failed",
                RestStatus.INTERNAL_SERVER_ERROR
            )
        }
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
        userAccess.validateUser(user)
        validateConfig(request.notificationConfig, user)
        val currentConfigDoc = operations.getNotificationConfig(request.configId)
        currentConfigDoc
            ?: run {
                Metrics.NOTIFICATIONS_CONFIG_UPDATE_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                throw OpenSearchStatusException(
                    "NotificationConfig ${request.configId} not found",
                    RestStatus.NOT_FOUND
                )
            }

        val currentMetadata = currentConfigDoc.configDoc.metadata
        if (!userAccess.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "Permission denied for NotificationConfig ${request.configId}",
                RestStatus.FORBIDDEN
            )
        }
        if (currentConfigDoc.configDoc.config.configType != request.notificationConfig.configType) {
            throw OpenSearchStatusException("Config type cannot be changed after creation", RestStatus.CONFLICT)
        }

        val newMetadata = currentMetadata.copy(lastUpdateTime = Instant.now())
        val newConfigData = NotificationConfigDoc(newMetadata, request.notificationConfig)
        if (!operations.updateNotificationConfig(request.configId, newConfigData)) {
            Metrics.NOTIFICATIONS_CONFIG_UPDATE_SYSTEM_ERROR.counter.increment()
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
        userAccess.validateUser(user)
        return when (request.configIds.size) {
            0 -> getAll(request, user)
            1 -> info(request.configIds.first(), user)
            else -> info(request.configIds, user)
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
        val configDoc = operations.getNotificationConfig(configId)
        configDoc
            ?: run {
                Metrics.NOTIFICATIONS_CONFIG_INFO_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                throw OpenSearchStatusException("NotificationConfig $configId not found", RestStatus.NOT_FOUND)
            }
        val metadata = configDoc.configDoc.metadata
        if (!userAccess.doesUserHasAccess(user, metadata.tenant, metadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
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
     * Get NotificationConfig info
     * @param configIds config id set
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun info(configIds: Set<String>, user: User?): GetNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-info $configIds")
        val configDocs = operations.getNotificationConfigs(configIds)
        if (configDocs.size != configIds.size) {
            val mutableSet = configIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.docInfo.id) }
            Metrics.NOTIFICATIONS_CONFIG_LIST_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "NotificationConfig $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            val currentMetadata = it.configDoc.metadata
            if (!userAccess.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
                Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "Permission denied for NotificationConfig ${it.docInfo.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val configSearchResult = configDocs.map {
            NotificationConfigInfo(
                it.docInfo.id!!,
                it.configDoc.metadata.lastUpdateTime,
                it.configDoc.metadata.createdTime,
                it.configDoc.metadata.tenant,
                it.configDoc.config
            )
        }
        return GetNotificationConfigResponse(NotificationConfigSearchResult(configSearchResult))
    }

    /**
     * Get all NotificationConfig matching the criteria
     * @param request [GetNotificationConfigRequest] object
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun getAll(request: GetNotificationConfigRequest, user: User?): GetNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-getAll")
        val searchResult = operations.getAllNotificationConfigs(
            userAccess.getUserTenant(user),
            userAccess.getSearchAccessInfo(user),
            request
        )
        return GetNotificationConfigResponse(searchResult)
    }

    /**
     * Get NotificationConfig info
     * @param request [GetFeatureChannelListRequest] object
     * @param user the user info object
     * @return [GetFeatureChannelListResponse]
     */
    fun getFeatureChannelList(request: GetFeatureChannelListRequest, user: User?): GetFeatureChannelListResponse {
        log.info("$LOG_PREFIX:getFeatureChannelList $request")
        userAccess.validateUser(user)
        val supportedChannelListString = getSupportedChannelList().joinToString(",")
        val filterParams = mapOf(
            Pair("feature_list", request.feature),
            Pair("config_type", supportedChannelListString)
        )
        val getAllRequest = GetNotificationConfigRequest(filterParams = filterParams)
        val getAllResult = operations.getAllNotificationConfigs(
            userAccess.getUserTenant(user),
            userAccess.getSearchAccessInfo(user),
            getAllRequest
        )
        val searchResult = getAllResult.objectList.map {
            val configId = it.configId
            val config = it.notificationConfig
            FeatureChannel(configId, config.name, config.description, config.configType, config.isEnabled)
        }
        val featureChannelList = FeatureChannelList(searchResult)
        return GetFeatureChannelListResponse(featureChannelList)
    }

    private fun getSupportedChannelList(): List<String> {
        return listOf(
            ConfigType.SLACK.tag,
            ConfigType.CHIME.tag,
            ConfigType.WEBHOOK.tag,
            ConfigType.EMAIL.tag
        )
    }

    /**
     * Delete NotificationConfig
     * @param configId NotificationConfig object id
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    private fun delete(configId: String, user: User?): DeleteNotificationConfigResponse {
        log.info("$LOG_PREFIX:NotificationConfig-delete $configId")
        userAccess.validateUser(user)
        val currentConfigDoc = operations.getNotificationConfig(configId)
        currentConfigDoc
            ?: run {
                Metrics.NOTIFICATIONS_CONFIG_DELETE_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                throw OpenSearchStatusException(
                    "NotificationConfig $configId not found",
                    RestStatus.NOT_FOUND
                )
            }

        val currentMetadata = currentConfigDoc.configDoc.metadata
        if (!userAccess.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "Permission denied for NotificationConfig $configId",
                RestStatus.FORBIDDEN
            )
        }
        if (!operations.deleteNotificationConfig(configId)) {
            Metrics.NOTIFICATIONS_CONFIG_DELETE_SYSTEM_ERROR.counter.increment()
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
        userAccess.validateUser(user)
        val configDocs = operations.getNotificationConfigs(configIds)
        if (configDocs.size != configIds.size) {
            val mutableSet = configIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.docInfo.id) }
            Metrics.NOTIFICATIONS_CONFIG_DELETE_LIST_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "NotificationConfig $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            val currentMetadata = it.configDoc.metadata
            if (!userAccess.doesUserHasAccess(user, currentMetadata.tenant, currentMetadata.access)) {
                Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "Permission denied for NotificationConfig ${it.docInfo.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val deleteStatus = operations.deleteNotificationConfigs(configIds)
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
