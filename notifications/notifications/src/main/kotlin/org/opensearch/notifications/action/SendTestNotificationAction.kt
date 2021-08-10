package org.opensearch.notifications.action

import kotlinx.coroutines.launch
import org.opensearch.OpenSearchSecurityException
import org.opensearch.OpenSearchStatusException
import org.opensearch.action.ActionListener
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.action.support.HandledTransportAction
import org.opensearch.client.Client
import org.opensearch.client.node.NodeClient
import org.opensearch.common.inject.Inject
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.ConfigConstants
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.NotificationsActions
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.recreateObject
import org.opensearch.index.IndexNotFoundException
import org.opensearch.index.engine.VersionConflictEngineException
import org.opensearch.indices.InvalidIndexNameException
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.model.SendTestNotificationRequest
import org.opensearch.notifications.model.SendTestNotificationResponse
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import java.io.IOException

/**
 * Send Notification transport action
 */
internal class SendTestNotificationAction @Inject constructor(
    transportService: TransportService,
    val client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry,
) : HandledTransportAction<SendTestNotificationRequest, SendTestNotificationResponse>(
    NAME,
    transportService,
    actionFilters,
    ::SendTestNotificationRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/notifications/send_test_message"
        internal val ACTION_TYPE = ActionType(NAME, ::SendTestNotificationResponse)
        private val log by logger(SendTestNotificationAction::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun doExecute(
        task: Task?,
        request: SendTestNotificationRequest,
        listener: ActionListener<SendTestNotificationResponse>
    ) {
        log.info("debug send test notification action do execute")
        NotificationsPluginInterface.sendNotification(
            client as NodeClient,
            request.eventSource,
            request.channelMessage,
            request.channelIds,
            object : ActionListener<SendNotificationResponse> {
                override fun onResponse(p0: SendNotificationResponse) {
                    log.info("$LOG_PREFIX:NotificationsActions-send:${p0.notificationId}")
                    listener.onResponse(SendTestNotificationResponse(p0))
                }

                override fun onFailure(p0: java.lang.Exception) {
                    log.error("$LOG_PREFIX:NotificationsActions-send Error:$p0")
                    listener.onFailure(p0)
                }
            }
        )
//        val userStr: String? =
//            client.threadPool().threadContext.getTransient<String>(ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
//        val user: User? = User.parse(userStr)
//        val storedThreadContext = client.threadPool().threadContext.newStoredContext(false)
//        try {
////                client.threadPool().threadContext.stashContext().use {
////                    storedThreadContext.restore()
////                    listener.onResponse(executeRequest(request as Request, user))
////                }
//            NotificationsPluginInterface.sendNotification(
//                client as NodeClient,
//                request.eventSource,
//                request.channelMessage,
//                request.channelIds,
//                listener
//            )
//        } catch (exception: OpenSearchStatusException) {
//            log.warn("$LOG_PREFIX:OpenSearchStatusException:", exception)
//            listener.onFailure(exception)
//        } catch (exception: OpenSearchSecurityException) {
//            log.warn("$LOG_PREFIX:OpenSearchSecurityException:", exception)
//            listener.onFailure(
//                OpenSearchStatusException(
//                    "Permissions denied: ${exception.message} - Contact administrator",
//                    RestStatus.FORBIDDEN
//                )
//            )
//        } catch (exception: VersionConflictEngineException) {
//            log.warn("$LOG_PREFIX:VersionConflictEngineException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.CONFLICT))
//        } catch (exception: IndexNotFoundException) {
//            log.warn("$LOG_PREFIX:IndexNotFoundException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.NOT_FOUND))
//        } catch (exception: InvalidIndexNameException) {
//            log.warn("$LOG_PREFIX:InvalidIndexNameException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.BAD_REQUEST))
//        } catch (exception: IllegalArgumentException) {
//            log.warn("$LOG_PREFIX:IllegalArgumentException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.BAD_REQUEST))
//        } catch (exception: IllegalStateException) {
//            log.warn("$LOG_PREFIX:IllegalStateException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.SERVICE_UNAVAILABLE))
//        } catch (exception: IOException) {
//            log.error("$LOG_PREFIX:Uncaught IOException:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.FAILED_DEPENDENCY))
//        } catch (exception: Exception) {
//            log.error("$LOG_PREFIX:Uncaught Exception:", exception)
//            listener.onFailure(OpenSearchStatusException(exception.message, RestStatus.INTERNAL_SERVER_ERROR))
//        }
//    }
//
//
//    /**
//     * {@inheritDoc}
//     */
//    override fun executeRequest(
//        request: SendTestNotificationRequest,
//        user: User?
//    ): SendTestNotificationResponse {
//        val log by logger(Exception::class.java)
//        log.info("debug send test notification action execute request")
//        NotificationsPluginInterface.sendNotification(
//            client as NodeClient,
//            request.eventSource,
//            request.channelMessage,
//            request.channelIds,
//            object : ActionListener<SendTestNotificationResponse> {
//                override fun onResponse(p0: SendTestNotificationResponse) {
//                    log.info("NotificationsActions-send:${p0.notificationId}")
//                }
//
//                override fun onFailure(p0: java.lang.Exception) {
//                    log.error("NotificationsActions-send Error:$p0")
//                }
//            }
//        )
//    }
    }
}
