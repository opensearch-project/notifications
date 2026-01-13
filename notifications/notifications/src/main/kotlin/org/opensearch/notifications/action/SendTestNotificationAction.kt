/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.action.support.HandledTransportAction
import org.opensearch.common.inject.Inject
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.utils.logger
import org.opensearch.core.action.ActionListener
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.model.SendTestNotificationRequest
import org.opensearch.notifications.send.SendTestNotificationActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.node.NodeClient

/**
 * Send Test Notification transport action
 */
internal class SendTestNotificationAction
    @Inject
    constructor(
        transportService: TransportService,
        val client: Client,
        actionFilters: ActionFilters,
        val xContentRegistry: NamedXContentRegistry,
    ) : HandledTransportAction<SendTestNotificationRequest, SendNotificationResponse>(
            NAME,
            transportService,
            actionFilters,
            ::SendTestNotificationRequest,
        ) {
        companion object {
            internal const val NAME = "cluster:admin/opensearch/notifications/test_notification"
            internal val ACTION_TYPE = ActionType(NAME, ::SendNotificationResponse)
            private val log by logger(SendTestNotificationAction::class.java)
        }

        /**
         * {@inheritDoc}
         */
        override fun doExecute(
            task: Task?,
            request: SendTestNotificationRequest,
            listener: ActionListener<SendNotificationResponse>,
        ) {
            val source = SendTestNotificationActionHelper.generateEventSource(request.configId)
            val message = SendTestNotificationActionHelper.generateMessage(request.configId)
            val channelIds = listOf(request.configId)
            NotificationsPluginInterface.sendNotification(
                client as NodeClient,
                source,
                message,
                channelIds,
                object : ActionListener<SendNotificationResponse> {
                    override fun onResponse(sendNotificationResponse: SendNotificationResponse) {
                        log.info("$LOG_PREFIX:SendTestNotificationAction-send:${sendNotificationResponse.notificationEvent}")
                        listener.onResponse(sendNotificationResponse)
                    }

                    override fun onFailure(exception: Exception) {
                        log.error("$LOG_PREFIX:SendTestNotificationAction-send Error:$exception")
                        listener.onFailure(exception)
                    }
                },
            )
        }
    }
