package org.opensearch.notifications.action

import org.opensearch.action.ActionListener
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.action.support.HandledTransportAction
import org.opensearch.client.Client
import org.opensearch.client.node.NodeClient
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.model.SendTestNotificationRequest
import org.opensearch.notifications.send.SendTestNotificationActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService

/**
 * Send Test Notification transport action
 */
internal class SendTestNotificationAction @Inject constructor(
    transportService: TransportService,
    val client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry,
) : HandledTransportAction<SendTestNotificationRequest, SendNotificationResponse>(
    NAME,
    transportService,
    actionFilters,
    ::SendTestNotificationRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/notifications/test_notification"
        internal val ACTION_TYPE = ActionType(NAME, ::SendNotificationResponse)
        private val log by logger(SendTestNotificationAction::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun doExecute(
        task: Task?,
        request: SendTestNotificationRequest,
        listener: ActionListener<SendNotificationResponse>
    ) {
        val source = SendTestNotificationActionHelper.generateEventSource(request.feature, request.configId)
        val message = SendTestNotificationActionHelper.generateMessage(request.feature, request.configId)
        val channelIds = listOf(request.configId)
        NotificationsPluginInterface.sendNotification(
            client as NodeClient,
            source,
            message,
            channelIds,
            object : ActionListener<SendNotificationResponse> {
                override fun onResponse(p0: SendNotificationResponse) {
                    log.info("$LOG_PREFIX:SendTestNotificationAction-send:${p0.notificationId}")
                    listener.onResponse(p0)
                }

                override fun onFailure(p0: java.lang.Exception) {
                    log.error("$LOG_PREFIX:SendTestNotificationAction-send Error:$p0")
                    listener.onFailure(p0)
                }
            }
        )
    }
}
