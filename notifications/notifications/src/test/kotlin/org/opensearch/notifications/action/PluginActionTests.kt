/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.action

import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.commons.destination.response.LegacyDestinationResponse
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetChannelListRequest
import org.opensearch.commons.notifications.action.GetChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.commons.notifications.action.GetPluginFeaturesResponse
import org.opensearch.commons.notifications.action.LegacyPublishNotificationRequest
import org.opensearch.commons.notifications.action.LegacyPublishNotificationResponse
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.ChannelList
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.core.action.ActionListener
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.tasks.Task
import org.opensearch.transport.TransportService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class PluginActionTests {

    @Mock
    private lateinit var transportService: TransportService

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: Client

    @Mock
    private lateinit var xContentRegistry: NamedXContentRegistry

    @Mock
    private lateinit var task: Task

    private val actionFilters = ActionFilters(setOf())

    @Test
    fun `Create notification config action should call back action listener`() {
        val notificationId = "notification-1"
        val request = mock(CreateNotificationConfigRequest::class.java)
        val response = CreateNotificationConfigResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every {
            runBlocking {
                ConfigIndexingActions.create(request, any())
            }
        } returns response

        val createNotificationConfigAction = CreateNotificationConfigAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        createNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Update notification config action should call back action listener`() {
        val notificationId = "notification-1"
        val request = mock(UpdateNotificationConfigRequest::class.java)
        val response = UpdateNotificationConfigResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every {
            runBlocking {
                ConfigIndexingActions.update(request, any())
            }
        } returns response

        val updateNotificationConfigAction = UpdateNotificationConfigAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        updateNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Delete notification config action should call back action listener`() {
        val request = mock(DeleteNotificationConfigRequest::class.java)
        val response = DeleteNotificationConfigResponse(
            mapOf(Pair("sample_config_id", RestStatus.OK))
        )

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every {
            runBlocking {
                ConfigIndexingActions.delete(request, any())
            }
        } returns response

        val deleteNotificationConfigAction = DeleteNotificationConfigAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        deleteNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get notification config action should call back action listener`() {
        val request = mock(GetNotificationConfigRequest::class.java)
        val response = GetNotificationConfigResponse(
            mock(NotificationConfigSearchResult::class.java)
        )

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every {
            runBlocking {
                ConfigIndexingActions.get(request, any())
            }
        } returns response

        val getNotificationConfigAction = GetNotificationConfigAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        getNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get plugin features action should call back action listener`() {
        val allowedConfigTypes = listOf("type1")
        val pluginFeatures = mapOf(Pair("FeatureKey1", "Feature1"))
        val request = mock(GetPluginFeaturesRequest::class.java)
        val response = GetPluginFeaturesResponse(allowedConfigTypes, pluginFeatures)

        val getPluginFeaturesAction = GetPluginFeaturesAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        getPluginFeaturesAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get channel list action should call back action listener`() {
        val request = mock(GetChannelListRequest::class.java)
        val response = GetChannelListResponse(mock(ChannelList::class.java))

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every {
            runBlocking {
                ConfigIndexingActions.getChannelList(request, any())
            }
        } returns response

        val getChannelListAction = GetChannelListAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        getChannelListAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Send notification action should call back action listener`() {
        val notificationId = "notification-1"
        val request = mock(SendNotificationRequest::class.java)

        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            severity = SeverityType.INFO
        )
        val sampleStatus = EventStatus(
            "config_id",
            "name",
            ConfigType.SLACK,
            deliveryStatus = DeliveryStatus("404", "invalid recipient")
        )

        val sampleEvent = NotificationEvent(sampleEventSource, listOf(sampleStatus))

        val response = SendNotificationResponse(sampleEvent)

        // Mock singleton's method by mockk framework
        mockkObject(SendMessageActionHelper)
        every {
            runBlocking {
                SendMessageActionHelper.executeRequest(request)
            }
        } returns response

        val sendNotificationAction = SendNotificationAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        sendNotificationAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Publish notification action should call back action listener`() {
        val request = mock(LegacyPublishNotificationRequest::class.java)
        val response = LegacyPublishNotificationResponse(
            LegacyDestinationResponse.Builder().withStatusCode(200).withResponseContent("Hello world").build()
        )

        // Mock singleton's method by mockk framework
        mockkObject(SendMessageActionHelper)
        every { SendMessageActionHelper.executeLegacyRequest(request) } returns response

        val publishNotificationAction = PublishNotificationAction(
            transportService,
            client,
            actionFilters,
            xContentRegistry
        )
        publishNotificationAction.execute(task, request, AssertionListener(response))
    }

    /**
     * This listener class is to assert on response rather than verify it called.
     * The reason why this is required is because it is harder to do the latter
     * (verify listener being called once) due to CoroutineScope used in execute()
     */
    private class AssertionListener<Response : BaseResponse>(
        val expected: Response
    ) : ActionListener<Response> {

        override fun onResponse(actual: Response?) {
            assertEquals(expected, actual)
        }

        override fun onFailure(error: Exception?) {
            fail("Unexpected error happened", error)
        }
    }
}
