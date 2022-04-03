/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.send

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.opensearch.OpenSearchStatusException
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.commons.authuser.User
import org.opensearch.commons.destination.message.LegacyBaseMessage
import org.opensearch.commons.destination.message.LegacyCustomWebhookMessage
import org.opensearch.commons.destination.message.LegacyDestinationType
import org.opensearch.commons.destination.response.LegacyDestinationResponse
import org.opensearch.commons.notifications.action.LegacyPublishNotificationRequest
import org.opensearch.commons.notifications.action.LegacyPublishNotificationResponse
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.EmailRecipientStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SesAccount
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Sns
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.CoreProvider
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.ConfigOperations
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.security.UserAccess
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.BaseDestination
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.SesDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.notifications.spi.model.destination.SmtpDestination
import org.opensearch.notifications.spi.model.destination.SnsDestination
import org.opensearch.rest.RestStatus
import java.io.ByteArrayOutputStream
import java.time.Instant

/**
 * Helper function for send transport action.
 */
@Suppress("TooManyFunctions")
object SendMessageActionHelper {
    private val log by logger(SendMessageActionHelper::class.java)

    private lateinit var configOperations: ConfigOperations
    private lateinit var userAccess: UserAccess

    fun initialize(configOperations: ConfigOperations, userAccess: UserAccess) {
        this.configOperations = configOperations
        this.userAccess = userAccess
    }

    /**
     * Send notification message and keep audit.
     * @param request request object
     */
    fun executeRequest(request: SendNotificationRequest): SendNotificationResponse {
        val eventSource = request.eventSource
        val channelMessage = request.channelMessage
        val channelIds = request.channelIds.toSet()
        val user: User? = User.parse(request.threadContext)
        val createdTime = Instant.now()
        userAccess.validateUser(user)
        val channelMap = getConfigs(channelIds)
        val childConfigMap = getConfigs(getChildConfigIds(channelMap.values.filterNotNull().toList()))
        val message = createMessageContent(eventSource, channelMessage)
        val eventStatusList = sendMessagesInParallel(user, eventSource, channelMap, childConfigMap, message)
        val updatedTime = Instant.now()
        val docMetadata = DocMetadata(
            updatedTime,
            createdTime,
            userAccess.getAllAccessInfo(user)
        )
        val event = NotificationEvent(eventSource, eventStatusList)
        val eventDoc = NotificationEventDoc(event)
        val docId = "test_doc"
        // TODO: Add eventDoc in the response of NotificationResponse
//        val docId = eventOperations.createNotificationEvent(eventDoc)
//            ?: run {
//                Metrics.NOTIFICATIONS_SEND_MESSAGE_SYSTEM_ERROR.counter.increment()
//                throw OpenSearchStatusException("Indexing not Acknowledged", RestStatus.INSUFFICIENT_STORAGE)
//            }
        // traverse status to determine HTTP status code
        var overallStatusCode = eventStatusList.first().deliveryStatus?.statusCode
        eventStatusList.forEach { eventStatus ->
            if (eventStatus.deliveryStatus?.statusCode != overallStatusCode) {
                overallStatusCode = RestStatus.MULTI_STATUS.status.toString()
            }
        }
        val eventStatusListString = eventStatusList.joinToString(",", "[", "]") { getJsonString(it) }
        if (overallStatusCode != RestStatus.OK.status.toString()) {
            val errorMessage = "{\"notification_id\": \"$docId\",\"event_status_list\": $eventStatusListString}"
            throw OpenSearchStatusException(
                errorMessage, RestStatus.fromCode(overallStatusCode!!.toInt())
            )
        }

        return SendNotificationResponse(docId)
    }

    /**
     * Send legacy notification message intended only for Index Management plugin.
     * @param request request object
     */
    fun executeLegacyRequest(request: LegacyPublishNotificationRequest): LegacyPublishNotificationResponse {
        val baseMessage = request.baseMessage
        val response: LegacyDestinationResponse
        runBlocking {
            response = sendMessageToLegacyDestination(baseMessage)
        }
        return LegacyPublishNotificationResponse(response)
    }

    /**
     * Create message content from the request parameters
     * @param eventSource event source of request
     * @param channelMessage channel message of request
     * @return created message content object
     */
    private fun createMessageContent(eventSource: EventSource, channelMessage: ChannelMessage): MessageContent {
        return MessageContent(
            eventSource.title,
            channelMessage.textDescription,
            channelMessage.htmlDescription,
            channelMessage.attachment?.fileName,
            channelMessage.attachment?.fileEncoding,
            channelMessage.attachment?.fileData,
            channelMessage.attachment?.fileContentType,
        )
    }

    /**
     * Send message to multiple channels in parallel
     * @param eventSource event source information
     * @param channelMap map of channel id to channel info
     * @param childConfigMap map of config id to configuration info of child for compound channels like email
     * @param message the message to send
     * @return notification delivery status for each channel
     */
    private fun sendMessagesInParallel(
        user: User?,
        eventSource: EventSource,
        channelMap: Map<String, NotificationConfigDocInfo?>,
        childConfigMap: Map<String, NotificationConfigDocInfo?>,
        message: MessageContent
    ): List<EventStatus> {
        val statusList: List<EventStatus>
        // Fire all the message sending in parallel
        runBlocking {
            val statusDeferredList = channelMap.map {
                async(Dispatchers.IO) { sendMessageToChannel(user, eventSource, it, childConfigMap, message) }
            }
            statusList = statusDeferredList.awaitAll()
        }
        return statusList
    }

    /**
     * Send message to a channel
     * @param eventSource event source information
     * @param channelEntry channel info
     * @param childConfigMap configuration info of child for compound channels like email
     * @param message the message to send
     * @return notification delivery status for the channel
     */
    private fun sendMessageToChannel(
        user: User?,
        eventSource: EventSource,
        channelEntry: Map.Entry<String, NotificationConfigDocInfo?>,
        childConfigMap: Map<String, NotificationConfigDocInfo?>,
        message: MessageContent
    ): EventStatus {
        Metrics.NOTIFICATIONS_SEND_MESSAGE_TOTAL.counter.increment()
        Metrics.NOTIFICATIONS_SEND_MESSAGE_INTERVAL_COUNT.counter.increment()
        if (channelEntry.value == null) {
            Metrics.NOTIFICATIONS_SEND_MESSAGE_USER_ERROR_NOT_FOUND.counter.increment()
            return EventStatus(
                channelEntry.key,
                "invalid-config",
                ConfigType.NONE,
                listOf(),
                DeliveryStatus(RestStatus.NOT_FOUND.status.toString(), "Channel ${channelEntry.key} not found")
            )
        } else if (!userAccess.doesUserHaveSendAccess(user, channelEntry.value!!.configDoc.metadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
            return EventStatus(
                channelEntry.key,
                "invalid-access",
                ConfigType.NONE,
                listOf(),
                DeliveryStatus(RestStatus.FORBIDDEN.status.toString(), "Access denied for channel ${channelEntry.key}")
            )
        }
        val channel = channelEntry.value!!
        val configType = channel.configDoc.config.configType
        val configData = channel.configDoc.config.configData
        var emailRecipientStatus = listOf<EmailRecipientStatus>()
        if (configType == ConfigType.EMAIL) {
            emailRecipientStatus =
                listOf(
                    EmailRecipientStatus(
                        "placeholder@example.com",
                        DeliveryStatus("Scheduled", "Pending execution")
                    )
                )
        }
        val eventStatus = EventStatus(
            channel.docInfo.id!!, // ID from query so not expected to be null
            channel.configDoc.config.name,
            channel.configDoc.config.configType,
            emailRecipientStatus,
            DeliveryStatus("Scheduled", "Pending execution")
        )
        val invalidStatus: DeliveryStatus? = getStatusIfChannelIsNotEligibleToSendMessage(channel)
        if (invalidStatus != null) {
            return eventStatus.copy(deliveryStatus = invalidStatus)
        }

        val response = when (configType) {
            ConfigType.NONE -> null
            ConfigType.SLACK -> sendSlackMessage(configData as Slack, message, eventStatus, eventSource.referenceId)
            ConfigType.CHIME -> sendChimeMessage(configData as Chime, message, eventStatus, eventSource.referenceId)
            ConfigType.WEBHOOK -> sendWebhookMessage(
                configData as Webhook,
                message,
                eventStatus,
                eventSource.referenceId
            )
            ConfigType.EMAIL -> sendEmailMessage(
                user,
                configData as Email,
                childConfigMap,
                message,
                eventStatus,
                eventSource.referenceId
            )
            ConfigType.SES_ACCOUNT -> null
            ConfigType.SMTP_ACCOUNT -> null
            ConfigType.EMAIL_GROUP -> null
            ConfigType.SNS -> sendSNSMessage(configData as Sns, message, eventStatus, eventSource.referenceId)
        }
        return if (response == null) {
            log.warn("Cannot send message to destination for config id :${channel.docInfo.id}")
            Metrics.NOTIFICATIONS_SEND_MESSAGE_USER_ERROR_NOT_FOUND.counter.increment()
            eventStatus.copy(deliveryStatus = DeliveryStatus(RestStatus.NOT_FOUND.name, "Channel not found"))
        } else {
            response
        }
    }

    /**
     * Send message to a legacy destination intended only for Alerting and Index Management
     *
     * Currently this simply converts the legacy base message to the equivalent destination classes that exist
     * for the notification channels and utilizes the [sendMessageThroughSpi] method. If we get to the point
     * where this method seems to be holding back notification channels from adding new functionality we can
     * refactor this to have its own internal private core call to completely decouple them instead.
     *
     * @param baseMessage legacy base message
     * @return notification delivery status for the legacy destination
     */
    private fun sendMessageToLegacyDestination(baseMessage: LegacyBaseMessage): LegacyDestinationResponse {
        val message =
            MessageContent(title = "Legacy Notification", textDescription = baseMessage.messageContent)
        // These legacy destination calls do not have reference Ids, just passing 'legacy' constant
        return when (baseMessage.channelType) {
            LegacyDestinationType.LEGACY_SLACK -> {
                val destination = SlackDestination(baseMessage.url)
                val status = sendMessageThroughSpi(destination, message, "legacy")
                LegacyDestinationResponse.Builder().withStatusCode(status.statusCode)
                    .withResponseContent(status.statusText).build()
            }
            LegacyDestinationType.LEGACY_CHIME -> {
                val destination = ChimeDestination(baseMessage.url)
                val status = sendMessageThroughSpi(destination, message, "legacy")
                LegacyDestinationResponse.Builder().withStatusCode(status.statusCode)
                    .withResponseContent(status.statusText).build()
            }
            LegacyDestinationType.LEGACY_CUSTOM_WEBHOOK -> {
                val destination = CustomWebhookDestination(
                    (baseMessage as LegacyCustomWebhookMessage).uri.toString(),
                    baseMessage.headerParams,
                    baseMessage.method
                )
                val status = sendMessageThroughSpi(destination, message, "legacy")
                LegacyDestinationResponse.Builder().withStatusCode(status.statusCode)
                    .withResponseContent(status.statusText).build()
            }
            null -> {
                log.warn("No channel type given (null) for publishing to legacy destination")
                LegacyDestinationResponse.Builder().withStatusCode(400)
                    .withResponseContent("No channel type given (null) for publishing to legacy destination").build()
            }
        }
    }

    /**
     * Check if channel is eligible to send message, return error status if not
     * @param channel channel info
     * @return null if channel is eligible to send message. error delivery status if not
     */
    private fun getStatusIfChannelIsNotEligibleToSendMessage(
        channel: NotificationConfigDocInfo
    ): DeliveryStatus? {
        return if (!channel.configDoc.config.isEnabled) {
            DeliveryStatus(RestStatus.LOCKED.name, "The channel is muted")
        } else {
            null
        }
    }

    /**
     * send message to slack destination
     */
    private fun sendSlackMessage(
        slack: Slack,
        message: MessageContent,
        eventStatus: EventStatus,
        referenceId: String
    ): EventStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_SLACK.counter.increment()
        val destination = SlackDestination(slack.url)
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to chime destination
     */
    private fun sendChimeMessage(
        chime: Chime,
        message: MessageContent,
        eventStatus: EventStatus,
        referenceId: String
    ): EventStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_CHIME.counter.increment()
        val destination = ChimeDestination(chime.url)
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to custom webhook destination
     */
    private fun sendWebhookMessage(
        webhook: Webhook,
        message: MessageContent,
        eventStatus: EventStatus,
        referenceId: String
    ): EventStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_WEBHOOK.counter.increment()
        val destination = CustomWebhookDestination(webhook.url, webhook.headerParams, webhook.method.tag)
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to email destination
     */
    private fun sendEmailMessage(
        user: User?,
        email: Email,
        childConfigMap: Map<String, NotificationConfigDocInfo?>,
        message: MessageContent,
        eventStatus: EventStatus,
        referenceId: String
    ): EventStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_EMAIL.counter.increment()
        val accountDocInfo = childConfigMap[email.emailAccountID]
        if (accountDocInfo == null) {
            Metrics.NOTIFICATIONS_SEND_MESSAGE_USER_ERROR_NOT_FOUND.counter.increment()
            return eventStatus.copy(
                emailRecipientStatus = listOf(),
                deliveryStatus = DeliveryStatus(
                    RestStatus.NOT_FOUND.status.toString(),
                    "Sender ${email.emailAccountID} not found"
                )
            )
        } else if (!userAccess.doesUserHaveSendAccess(user, accountDocInfo.configDoc.metadata.access)) {
            Metrics.NOTIFICATIONS_PERMISSION_USER_ERROR.counter.increment()
            return eventStatus.copy(
                emailRecipientStatus = listOf(),
                deliveryStatus = DeliveryStatus(
                    RestStatus.FORBIDDEN.status.toString(),
                    "Access denied for sender ${accountDocInfo.docInfo.id}"
                )
            )
        }
        val accessDeniedGroupIds = childConfigMap.filterValues {
            it != null && !userAccess.doesUserHaveSendAccess(user, it.configDoc.metadata.access)
        }.keys
        val invalidGroupIds = childConfigMap.filterValues { it == null }.keys
        val groups = childConfigMap.values.filterNotNull()
            .filter { email.emailGroupIds.contains(it.docInfo.id) && !accessDeniedGroupIds.contains(it.docInfo.id) }
        val groupRecipients = groups.map { (it.configDoc.config.configData as EmailGroup).recipients }.flatten()
        val recipients = email.recipients.union(groupRecipients)
        val emailRecipientStatus: List<EmailRecipientStatus>
        val accountConfig = accountDocInfo.configDoc.config
        runBlocking {
            val statusDeferredList = recipients.map {
                async(Dispatchers.IO) {
                    when (accountConfig.configType) {
                        ConfigType.SMTP_ACCOUNT -> sendEmailFromSmtpAccount(
                            accountConfig.name,
                            accountConfig.configData as SmtpAccount,
                            it.recipient,
                            message,
                            referenceId
                        )
                        ConfigType.SES_ACCOUNT -> sendEmailFromSesAccount(
                            accountConfig.name,
                            accountConfig.configData as SesAccount,
                            it.recipient,
                            message,
                            referenceId
                        )
                        else -> EmailRecipientStatus(
                            it.recipient,
                            DeliveryStatus(RestStatus.NOT_ACCEPTABLE.name, "email account type not enabled")
                        )
                    }
                }
            }
            emailRecipientStatus = statusDeferredList.awaitAll() + invalidGroupIds.map {
                EmailRecipientStatus(
                    "unknown-recipient@example.com",
                    DeliveryStatus(RestStatus.NOT_FOUND.status.toString(), "Recipient $it not found")
                )
            } + accessDeniedGroupIds.map {
                EmailRecipientStatus(
                    "invalid-access@example.com",
                    DeliveryStatus(RestStatus.FORBIDDEN.status.toString(), "Access denied for recipient $it")
                )
            }
        }
        val firstStatus = emailRecipientStatus.first()
        var overallStatus = firstStatus.deliveryStatus.statusCode
        var overallStatusText = firstStatus.deliveryStatus.statusText
        emailRecipientStatus.forEach {
            val status = it.deliveryStatus
            log.info("$LOG_PREFIX:${email.emailAccountID}:statusCode=${status.statusCode}, statusText=${status.statusText}")
            if (overallStatus != status.statusCode) {
                overallStatus = RestStatus.MULTI_STATUS.status.toString()
                overallStatusText = "Errors"
            }
        }
        return eventStatus.copy(
            emailRecipientStatus = emailRecipientStatus,
            deliveryStatus = DeliveryStatus(overallStatus, overallStatusText)
        )
    }

    /**
     * send message to smtp destination
     */
    private fun sendEmailFromSmtpAccount(
        accountName: String,
        smtpAccount: SmtpAccount,
        recipient: String,
        message: MessageContent,
        referenceId: String
    ): EmailRecipientStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_SMTP_ACCOUNT.counter.increment()
        val destination = SmtpDestination(
            accountName,
            smtpAccount.host,
            smtpAccount.port,
            smtpAccount.method.tag,
            smtpAccount.fromAddress,
            recipient
        )
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return EmailRecipientStatus(
            recipient,
            DeliveryStatus(status.statusCode.toString(), status.statusText)
        )
    }

    /**
     * send message to ses destination
     */
    private fun sendEmailFromSesAccount(
        accountName: String,
        sesAccount: SesAccount,
        recipient: String,
        message: MessageContent,
        referenceId: String
    ): EmailRecipientStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_SES_ACCOUNT.counter.increment()
        val destination = SesDestination(
            accountName,
            sesAccount.awsRegion,
            sesAccount.roleArn,
            sesAccount.fromAddress,
            recipient
        )
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return EmailRecipientStatus(
            recipient,
            DeliveryStatus(status.statusCode.toString(), status.statusText)
        )
    }

    /**
     * send message to SNS destination
     */
    private fun sendSNSMessage(
        sns: Sns,
        message: MessageContent,
        eventStatus: EventStatus,
        referenceId: String
    ): EventStatus {
        Metrics.NOTIFICATIONS_MESSAGE_DESTINATION_SNS.counter.increment()
        val destination = SnsDestination(sns.topicArn, sns.roleArn)
        val status = sendMessageThroughSpi(destination, message, referenceId)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * Send message to destination using SPI
     */
    @Suppress("TooGenericExceptionCaught", "UnusedPrivateMember")
    private fun sendMessageThroughSpi(
        destination: BaseDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        return try {
            val status = CoreProvider.core.sendMessage(destination, message, referenceId)
            log.info("$LOG_PREFIX:sendMessage:statusCode=${status.statusCode}, statusText=${status.statusText}")
            status
        } catch (exception: Exception) {
            log.warn("sendMessage Exception:$exception")
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, "Failed to send notification")
        }
    }

    /**
     * Collects all child configs of the channel configurations (like email)
     * @param channels list of NotificationConfigDocInfo
     * @return set of child config ids or empty set if not present
     */
    private fun getChildConfigIds(channels: List<NotificationConfigDocInfo>): Set<String> {
        val emailConfigs = channels.filter { it.configDoc.config.configType == ConfigType.EMAIL }
        return if (emailConfigs.isNotEmpty()) {
            val childIds = mutableSetOf<String>()
            emailConfigs.forEach {
                val email = it.configDoc.config.configData as Email
                childIds.add(email.emailAccountID)
                childIds.addAll(email.emailGroupIds)
            }
            return childIds
        } else {
            setOf()
        }
    }

    /**
     * Get NotificationConfig info
     * @param configIds config id set
     * @return map of config id to [NotificationConfigDocInfo]
     */
    private fun getConfigs(configIds: Set<String>): Map<String, NotificationConfigDocInfo?> {
        return when (configIds.size) {
            0 -> emptyMap()
            1 -> getSingleConfig(configIds.first())
            else -> getAllConfigs(configIds)
        }
    }

    /**
     * Get NotificationConfig info
     * @param configIds config id set
     * @return map of config id to [NotificationConfigDocInfo]
     */
    private fun getAllConfigs(configIds: Set<String>): Map<String, NotificationConfigDocInfo?> {
        log.info("$LOG_PREFIX:getAllConfigs-get $configIds")
        val configDocs = configOperations.getNotificationConfigs(configIds)
        val configMap = mutableMapOf<String, NotificationConfigDocInfo?>()
        configIds.forEach { configMap[it] = null }
        configDocs.forEach { configMap[it.docInfo.id!!] = it }
        if (configDocs.size != configIds.size) {
            val invalidConfigIds = configMap.filterValues { it == null }.keys
            log.error("$LOG_PREFIX:getAllConfigs $invalidConfigIds not found")
        }
        return configMap
    }

    /**
     * Get NotificationConfig info
     * @param configId config id
     * @return map of config id to [NotificationConfigDocInfo]
     */
    private fun getSingleConfig(configId: String): Map<String, NotificationConfigDocInfo?> {
        log.info("$LOG_PREFIX:getSingleConfig-get $configId")
        val configDoc = configOperations.getNotificationConfig(configId)
        return mapOf(configId to configDoc)
    }

    /**
     * Covert object to json String
     */
    private fun getJsonString(xContent: ToXContent): String {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
            xContent.toXContent(builder, ToXContent.EMPTY_PARAMS)
            builder.close()
            return byteArrayOutputStream.toString("UTF8")
        }
    }
}
