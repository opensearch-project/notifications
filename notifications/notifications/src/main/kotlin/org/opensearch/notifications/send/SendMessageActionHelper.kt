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
package org.opensearch.notifications.send

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
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
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.ConfigOperations
import org.opensearch.notifications.index.EventOperations
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.security.UserAccess
import org.opensearch.notifications.spi.NotificationSpi
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.BaseDestination
import org.opensearch.notifications.spi.model.destination.ChimeDestination
import org.opensearch.notifications.spi.model.destination.CustomWebhookDestination
import org.opensearch.notifications.spi.model.destination.SlackDestination
import org.opensearch.rest.RestStatus
import java.time.Instant

/**
 * Helper function for send transport action.
 */
@Suppress("TooManyFunctions")
object SendMessageActionHelper {
    private val log by logger(SendMessageActionHelper::class.java)

    private lateinit var configOperations: ConfigOperations
    private lateinit var eventOperations: EventOperations
    private lateinit var userAccess: UserAccess

    fun initialize(configOperations: ConfigOperations, eventOperations: EventOperations, userAccess: UserAccess) {
        this.configOperations = configOperations
        this.eventOperations = eventOperations
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
        val channels = getConfigs(channelIds)
        val childConfigs = getConfigs(getChildConfigIds(channels))
        val message = createMessageContent(eventSource, channelMessage)
        val eventStatusList = sendMessagesInParallel(eventSource, channels, childConfigs, message)
        val updatedTime = Instant.now()
        val docMetadata = DocMetadata(
            updatedTime,
            createdTime,
            userAccess.getUserTenant(user),
            userAccess.getAllAccessInfo(user)
        )
        val event = NotificationEvent(eventSource, eventStatusList)
        val eventDoc = NotificationEventDoc(docMetadata, event)
        val docId = eventOperations.createNotificationEvent(eventDoc)
            ?: throw OpenSearchStatusException("Indexing not Acknowledged", RestStatus.INSUFFICIENT_STORAGE)
        return SendNotificationResponse(docId)
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
     * @param channels list of channel info
     * @param childConfigs configuration info of child for compound channels like email
     * @param message the message to send
     * @return notification delivery status for each channel
     */
    private fun sendMessagesInParallel(
        eventSource: EventSource,
        channels: List<NotificationConfigDocInfo>,
        childConfigs: List<NotificationConfigDocInfo>,
        message: MessageContent
    ): List<EventStatus> {
        val statusList: List<EventStatus>
        // Fire all the message sending in parallel
        runBlocking {
            val statusDeferredList = channels.map {
                async(Dispatchers.IO) { sendMessageToChannel(eventSource, it, childConfigs, message) }
            }
            statusList = statusDeferredList.awaitAll()
        }
        return statusList
    }

    /**
     * Send message to a channel
     * @param eventSource event source information
     * @param channel channel info
     * @param childConfigs configuration info of child for compound channels like email
     * @param message the message to send
     * @return notification delivery status for the channel
     */
    private fun sendMessageToChannel(
        eventSource: EventSource,
        channel: NotificationConfigDocInfo,
        childConfigs: List<NotificationConfigDocInfo>,
        message: MessageContent
    ): EventStatus {
        val eventStatus = EventStatus(
            channel.docInfo.id!!, // ID from query so not expected to be null
            channel.configDoc.config.name,
            channel.configDoc.config.configType,
            listOf(),
            DeliveryStatus("Scheduled", "Pending execution")
        )
        val invalidStatus: DeliveryStatus? = getStatusIfChannelIsNotEligibleToSendMessage(eventSource, channel)
        if (invalidStatus != null) {
            return eventStatus.copy(deliveryStatus = invalidStatus)
        }
        val configType = channel.configDoc.config.configType
        val configData = channel.configDoc.config.configData
        val response = when (configType) {
            ConfigType.NONE -> null
            ConfigType.SLACK -> sendSlackMessage(configData as Slack, message, eventStatus)
            ConfigType.CHIME -> sendChimeMessage(configData as Chime, message, eventStatus)
            ConfigType.WEBHOOK -> sendWebhookMessage(configData as Webhook, message, eventStatus)
            ConfigType.EMAIL -> sendEmailMessage(configData as Email, childConfigs, message, eventStatus)
            ConfigType.SMTP_ACCOUNT -> null
            ConfigType.EMAIL_GROUP -> null
        }
        return if (response == null) {
            log.warn("Cannot send message to destination for config id :${channel.docInfo.id}")
            eventStatus.copy(deliveryStatus = DeliveryStatus(RestStatus.NOT_FOUND.name, "Channel not found"))
        } else {
            response
        }
    }

    /**
     * Check if channel is eligible to send message, return error status if not
     * @param eventSource event source information
     * @param channel channel info
     * @return null if channel is eligible to send message. error delivery status if not
     */
    private fun getStatusIfChannelIsNotEligibleToSendMessage(
        eventSource: EventSource,
        channel: NotificationConfigDocInfo
    ): DeliveryStatus? {
        return if (!channel.configDoc.config.isEnabled) {
            DeliveryStatus(RestStatus.LOCKED.name, "The channel is muted")
        } else if (!channel.configDoc.config.features.contains(eventSource.feature)) {
            DeliveryStatus(RestStatus.FORBIDDEN.name, "Feature is not enabled for channel")
        } else {
            null
        }
    }

    /**
     * send message to slack destination
     */
    private fun sendSlackMessage(slack: Slack, message: MessageContent, eventStatus: EventStatus): EventStatus {
        val destination = SlackDestination(slack.url)
        val status = sendMessageThroughSpi(destination, message)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to chime destination
     */
    private fun sendChimeMessage(chime: Chime, message: MessageContent, eventStatus: EventStatus): EventStatus {
        val destination = ChimeDestination(chime.url)
        val status = sendMessageThroughSpi(destination, message)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to custom webhook destination
     */
    private fun sendWebhookMessage(webhook: Webhook, message: MessageContent, eventStatus: EventStatus): EventStatus {
        val destination = CustomWebhookDestination(webhook.url, webhook.headerParams, webhook.method.tag)
        val status = sendMessageThroughSpi(destination, message)
        return eventStatus.copy(deliveryStatus = DeliveryStatus(status.statusCode.toString(), status.statusText))
    }

    /**
     * send message to email destination
     */
    private fun sendEmailMessage(
        email: Email,
        childConfigs: List<NotificationConfigDocInfo>,
        message: MessageContent,
        eventStatus: EventStatus
    ): EventStatus {
        val smtpAccount = childConfigs.find { it.docInfo.id == email.emailAccountID }
        val groups = childConfigs.filter { email.emailGroupIds.contains(it.docInfo.id) }
        val groupRecipients = groups.map { (it.configDoc.config.configData as EmailGroup).recipients }.flatten()
        val recipients = email.recipients.union(groupRecipients)
        val emailRecipientStatus: List<EmailRecipientStatus>
        runBlocking {
            val statusDeferredList = recipients.map {
                async(Dispatchers.IO) { sendEmailFromSmtpAccount(smtpAccount, it, message) }
            }
            emailRecipientStatus = statusDeferredList.awaitAll()
        }
        val firstStatus = emailRecipientStatus.first()
        var overallStatus = firstStatus.deliveryStatus.statusCode
        var overallStatusText = firstStatus.deliveryStatus.statusText
        emailRecipientStatus.forEach {
            val status = it.deliveryStatus
            log.info("$LOG_PREFIX:${email.emailAccountID}:statusCode=${status.statusCode}, statusText=${status.statusText}")
            if (overallStatus != status.statusCode) {
                overallStatus = RestStatus.MULTI_STATUS.name
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
    @Suppress("UnusedPrivateMember")
    private fun sendEmailFromSmtpAccount(
        smtpAccount: NotificationConfigDocInfo?,
        recipient: String,
        message: MessageContent
    ): EmailRecipientStatus {
        // TODO implement email channel conversion
        return EmailRecipientStatus(
            recipient,
            DeliveryStatus(RestStatus.NOT_IMPLEMENTED.name, "SMTP Channel not implemented")
        )
    }

    /**
     * Send message to destination using SPI
     */
    @Suppress("TooGenericExceptionCaught", "UnusedPrivateMember")
    private fun sendMessageThroughSpi(
        destination: BaseDestination,
        message: MessageContent
    ): DestinationMessageResponse {
        return try {
            val status = NotificationSpi.sendMessage(destination, message)
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
     * @return list of [NotificationConfigDocInfo]
     */
    private fun getConfigs(configIds: Set<String>): List<NotificationConfigDocInfo> {
        return when (configIds.size) {
            0 -> listOf()
            1 -> getSingleConfig(configIds.first())
            else -> getAllConfigs(configIds)
        }
    }

    /**
     * Get NotificationConfig info
     * @param configIds config id set
     * @return list of [NotificationConfigDocInfo]
     */
    private fun getAllConfigs(configIds: Set<String>): List<NotificationConfigDocInfo> {
        log.info("$LOG_PREFIX:getAllConfigs-get $configIds")
        val configDocs = configOperations.getNotificationConfigs(configIds)
        if (configDocs.size != configIds.size) {
            val mutableSet = configIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.docInfo.id) }
            throw OpenSearchStatusException(
                "getConfigs $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        return configDocs
    }

    /**
     * Get NotificationConfig info
     * @param configId config id
     * @return list of [NotificationConfigDocInfo]
     */
    private fun getSingleConfig(configId: String): List<NotificationConfigDocInfo> {
        log.info("$LOG_PREFIX:getSingleConfig-get $configId")
        val configDoc = configOperations.getNotificationConfig(configId)
            ?: throw OpenSearchStatusException("getConfigs $configId not found", RestStatus.NOT_FOUND)
        return listOf(configDoc)
    }
}
