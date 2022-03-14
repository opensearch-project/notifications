/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.index

import org.opensearch.OpenSearchStatusException
import org.opensearch.action.ActionListener
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.commons.notifications.action.GetChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.Channel
import org.opensearch.commons.notifications.model.ChannelList
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
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
import org.opensearch.notifications.model.NotificationConfigDocInfo
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

    private fun validateEmailConfig(email: Email, user: User?) {
        if (email.emailGroupIds.contains(email.emailAccountID)) {
            throw OpenSearchStatusException(
                "Config IDs ${email.emailAccountID} is in both emailAccountID and emailGroupIds",
                RestStatus.BAD_REQUEST
            )
        }
        val configIds = setOf(email.emailAccountID).union(email.emailGroupIds)
        operations.getNotificationConfigs(
            configIds,
            object : ActionListener<List<NotificationConfigDocInfo>> {
                override fun onResponse(configDocs: List<NotificationConfigDocInfo>) {

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
                            ConfigType.SES_ACCOUNT -> if (it.docInfo.id != email.emailAccountID) {
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
                        if (!userAccess.doesUserHaveAccess(user, currentMetadata.access)) {
                            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                            throw OpenSearchStatusException(
                                "Permission denied for NotificationConfig ${it.docInfo.id}",
                                RestStatus.FORBIDDEN
                            )
                        }
                    }
                }
                override fun onFailure(exception: Exception) {
                    log.error(exception.message)
                }
            }
        )
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
            ConfigType.EMAIL -> validateEmailConfig(config.configData as Email, user)
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
    fun create(
        request: CreateNotificationConfigRequest,
        user: User?,
        actionListener: ActionListener<CreateNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-create")
        userAccess.validateUser(user)
        validateConfig(request.notificationConfig, user)
        val currentTime = Instant.now()
        val metadata = DocMetadata(
            currentTime,
            currentTime,
            userAccess.getAllAccessInfo(user)
        )
        val configDoc = NotificationConfigDoc(metadata, request.notificationConfig)

        operations.createNotificationConfig(
            configDoc, request.configId,
            object : ActionListener<String> {
                override fun onResponse(docId: String) {
                    docId ?: run {
                        Metrics.NOTIFICATIONS_CONFIG_CREATE_SYSTEM_ERROR.counter.increment()
                        throw OpenSearchStatusException(
                            "NotificationConfig Creation failed",
                            RestStatus.INTERNAL_SERVER_ERROR
                        )
                    }
                    actionListener.onResponse(CreateNotificationConfigResponse(docId))
                }

                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * Update NotificationConfig
     * @param request [UpdateNotificationConfigRequest] object
     * @param user the user info object
     * @return [UpdateNotificationConfigResponse]
     */
    fun update(
        request: UpdateNotificationConfigRequest,
        user: User?,
        actionListener: ActionListener<UpdateNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-update ${request.configId}")

        userAccess.validateUser(user)
        validateConfig(request.notificationConfig, user)

        operations.getNotificationConfig(
            request.configId,
            object : ActionListener<NotificationConfigDocInfo> {
                override fun onResponse(currentConfigDoc: NotificationConfigDocInfo?) {

                    currentConfigDoc
                        ?: run {
                            Metrics.NOTIFICATIONS_CONFIG_UPDATE_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                            throw OpenSearchStatusException(
                                "NotificationConfig ${request.configId} not found",
                                RestStatus.NOT_FOUND
                            )
                        }

                    val currentMetadata = currentConfigDoc.configDoc.metadata
                    if (!userAccess.doesUserHaveAccess(user, currentMetadata.access)) {
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
                    operations.updateNotificationConfig(
                        request.configId,
                        newConfigData,
                        object : ActionListener<Boolean> {
                            override fun onResponse(bool: Boolean) {
                                if (!bool) {
                                    Metrics.NOTIFICATIONS_CONFIG_UPDATE_SYSTEM_ERROR.counter.increment()
                                    throw OpenSearchStatusException("NotificationConfig Update failed", RestStatus.INTERNAL_SERVER_ERROR)
                                }
                                actionListener.onResponse(UpdateNotificationConfigResponse(request.configId))
                            }
                            override fun onFailure(exception: Exception) {
                                actionListener.onFailure(exception)
                            }
                        }
                    )
                }
                override fun onFailure(ex: Exception) {
                    actionListener.onFailure(ex)
                }
            }
        )
    }

    /**
     * Get NotificationConfig info
     * @param request [GetNotificationConfigRequest] object
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    fun get(
        request: GetNotificationConfigRequest,
        user: User?,
        actionListener: ActionListener<GetNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-get $request")
        userAccess.validateUser(user)
        when (request.configIds.size) {
            0 -> getAll(request, user, actionListener)
            1 -> info(request.configIds.first(), user, actionListener)
            else -> info(request.configIds, user, actionListener)
        }
    }

    /**
     * Get NotificationConfig info
     * @param configId config id
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun info(
        configId: String,
        user: User?,
        actionListener: ActionListener<GetNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-info $configId")
        operations.getNotificationConfig(
            configId,
            object : ActionListener<NotificationConfigDocInfo> {
                override fun onResponse(configDoc: NotificationConfigDocInfo) {
                    configDoc
                        ?: run {
                            Metrics.NOTIFICATIONS_CONFIG_INFO_USER_ERROR_INVALID_CONFIG_ID.counter.increment()
                            throw OpenSearchStatusException("NotificationConfig $configId not found", RestStatus.NOT_FOUND)
                        }
                    val metadata = configDoc.configDoc.metadata
                    if (!userAccess.doesUserHaveAccess(user, metadata.access)) {
                        Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                        throw OpenSearchStatusException("Permission denied for NotificationConfig $configId", RestStatus.FORBIDDEN)
                    }
                    val configInfo = NotificationConfigInfo(
                        configId,
                        metadata.lastUpdateTime,
                        metadata.createdTime,
                        configDoc.configDoc.config
                    )
                    actionListener.onResponse(GetNotificationConfigResponse(NotificationConfigSearchResult(configInfo)))
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * Get NotificationConfig info
     * @param configIds config id set
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun info(
        configIds: Set<String>,
        user: User?,
        actionListener: ActionListener<GetNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-info $configIds")
        operations.getNotificationConfigs(
            configIds,
            object : ActionListener<List<NotificationConfigDocInfo>> {

                override fun onResponse(configDocs: List<NotificationConfigDocInfo>) {

                    if (configDocs.size != configIds.size) {
                        val mutableSet = configIds.toMutableSet()
                        configDocs.forEach { mutableSet.remove(it.docInfo.id) }
                        Metrics.NOTIFICATIONS_CONFIG_INFO_USER_ERROR_SET_NOT_FOUND.counter.increment()
                        throw OpenSearchStatusException(
                            "NotificationConfig $mutableSet not found",
                            RestStatus.NOT_FOUND
                        )
                    }
                    configDocs.forEach {
                        val currentMetadata = it.configDoc.metadata
                        if (!userAccess.doesUserHaveAccess(user, currentMetadata.access)) {
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
                            it.configDoc.config
                        )
                    }
                    actionListener.onResponse(GetNotificationConfigResponse(NotificationConfigSearchResult(configSearchResult)))
                }

                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * Get all NotificationConfig matching the criteria
     * @param request [GetNotificationConfigRequest] object
     * @param user the user info object
     * @return [GetNotificationConfigResponse]
     */
    private fun getAll(
        request: GetNotificationConfigRequest,
        user: User?,
        actionListener: ActionListener<GetNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-getAll")
        operations.getAllNotificationConfigs(
            userAccess.getSearchAccessInfo(user),
            request,
            object : ActionListener<NotificationConfigSearchResult> {
                override fun onResponse(searchResult: NotificationConfigSearchResult) {
                    actionListener.onResponse(GetNotificationConfigResponse(searchResult))
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * Get NotificationConfig info
     * @param request [GetChannelListRequest] object
     * @param user the user info object
     * @return [GetChannelListResponse]
     */
    fun getChannelList(
        request: GetChannelListRequest,
        user: User?,
        actionListener: ActionListener<GetChannelListResponse>
    ) {
        log.info("$LOG_PREFIX:getFeatureChannelList $request")
        userAccess.validateUser(user)
        val supportedChannelListString = getSupportedChannelList().joinToString(",")
        val filterParams = mapOf(
            Pair("config_type", supportedChannelListString)
        )
        val getAllRequest = GetNotificationConfigRequest(filterParams = filterParams)
        operations.getAllNotificationConfigs(
            userAccess.getSearchAccessInfo(user),
            getAllRequest,
            object : ActionListener<NotificationConfigSearchResult> {
                override fun onResponse(getAllResult: NotificationConfigSearchResult) {
                    val searchResult = getAllResult.objectList.map {
                        val configId = it.configId
                        val config = it.notificationConfig
                        Channel(configId, config.name, config.description, config.configType, config.isEnabled)
                    }
                    val featureChannelList = ChannelList(searchResult)
                    actionListener.onResponse(GetChannelListResponse(featureChannelList))
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    private fun getSupportedChannelList(): List<String> {
        return listOf(
            ConfigType.SLACK.tag,
            ConfigType.CHIME.tag,
            ConfigType.WEBHOOK.tag,
            ConfigType.EMAIL.tag,
            ConfigType.SNS.tag
        )
    }

    /**
     * Delete NotificationConfig
     * @param configIds NotificationConfig object ids
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    private fun delete(
        configIds: Set<String>,
        user: User?,
        actionListener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-delete $configIds")
        userAccess.validateUser(user)

        operations.getNotificationConfigs(
            configIds,
            object : ActionListener<List<NotificationConfigDocInfo>> {

                override fun onResponse(configDocs: List<NotificationConfigDocInfo>) {

                    if (configDocs.size != configIds.size) {
                        val mutableSet = configIds.toMutableSet()
                        configDocs.forEach { mutableSet.remove(it.docInfo.id) }
                        Metrics.NOTIFICATIONS_CONFIG_DELETE_USER_ERROR_SET_NOT_FOUND.counter.increment()
                        throw OpenSearchStatusException(
                            "NotificationConfig $mutableSet not found",
                            RestStatus.NOT_FOUND
                        )
                    }

                    configDocs.forEach {
                        val currentMetadata = it.configDoc.metadata
                        if (!userAccess.doesUserHaveAccess(user, currentMetadata.access)) {
                            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
                            throw OpenSearchStatusException(
                                "Permission denied for NotificationConfig ${it.docInfo.id}",
                                RestStatus.FORBIDDEN
                            )
                        }
                    }

                    operations.deleteNotificationConfigs(
                        configIds,
                        object : ActionListener<Map<String, RestStatus>> {
                            override fun onResponse(deleteStatus: Map<String, RestStatus>) {
                                actionListener.onResponse(DeleteNotificationConfigResponse(deleteStatus))
                            }
                            override fun onFailure(exception: Exception) {
                                actionListener.onFailure(exception)
                            }
                        }
                    )
                }

                override fun onFailure(ex: Exception) {
                    actionListener.onFailure(ex)
                }
            }
        )
    }

    /**
     * Delete NotificationConfig
     * @param request [DeleteNotificationConfigRequest] object
     * @param user the user info object
     * @return [DeleteNotificationConfigResponse]
     */
    fun delete(
        request: DeleteNotificationConfigRequest,
        user: User?,
        actionListener: ActionListener<DeleteNotificationConfigResponse>
    ) {
        log.info("$LOG_PREFIX:NotificationConfig-delete ${request.configIds}")
        delete(
            request.configIds, user,
            object : ActionListener<DeleteNotificationConfigResponse> {
                override fun onResponse(response: DeleteNotificationConfigResponse) {
                    actionListener.onResponse(response)
                }
                override fun onFailure(ex: Exception) {
                    actionListener.onFailure(ex)
                }
            }
        )
    }
}
