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
package org.opensearch.notifications.action

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.ActionListener
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.notifications.action.CreateNotificationConfigRequest
import org.opensearch.commons.notifications.action.CreateNotificationConfigResponse
import org.opensearch.commons.notifications.action.DeleteNotificationConfigRequest
import org.opensearch.commons.notifications.action.DeleteNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetFeatureChannelListRequest
import org.opensearch.commons.notifications.action.GetFeatureChannelListResponse
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.GetNotificationEventRequest
import org.opensearch.commons.notifications.action.GetNotificationEventResponse
import org.opensearch.commons.notifications.action.GetPluginFeaturesRequest
import org.opensearch.commons.notifications.action.GetPluginFeaturesResponse
import org.opensearch.commons.notifications.action.SendNotificationRequest
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.action.UpdateNotificationConfigRequest
import org.opensearch.commons.notifications.action.UpdateNotificationConfigResponse
import org.opensearch.commons.notifications.model.FeatureChannelList
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.NotificationEventSearchResult
import org.opensearch.notifications.index.ConfigIndexingActions
import org.opensearch.notifications.index.EventIndexingActions
import org.opensearch.notifications.send.SendMessageActionHelper
import org.opensearch.notifications.spi.NotificationSpi
import org.opensearch.rest.RestStatus
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

    private val actionFilters = ActionFilters(setOf())

    @Test
    fun `Create notification config action should call back action listener`() {
        val notificationId = "notification-1"
        val task = mock(Task::class.java)
        val request = mock(CreateNotificationConfigRequest::class.java)
        val response = CreateNotificationConfigResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every { ConfigIndexingActions.create(request, any()) } returns response

        val createNotificationConfigAction = CreateNotificationConfigAction(
            transportService, client, actionFilters, xContentRegistry
        )
        createNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Update notification config action should call back action listener`() {
        val notificationId = "notification-1"
        val task = mock(Task::class.java)
        val request = mock(UpdateNotificationConfigRequest::class.java)
        val response = UpdateNotificationConfigResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every { ConfigIndexingActions.update(request, any()) } returns response

        val updateNotificationConfigAction = UpdateNotificationConfigAction(
            transportService, client, actionFilters, xContentRegistry
        )
        updateNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Delete notification config action should call back action listener`() {
        val task = mock(Task::class.java)
        val request = mock(DeleteNotificationConfigRequest::class.java)
        val response = DeleteNotificationConfigResponse(
            mapOf(Pair("sample_config_id", RestStatus.OK))
        )

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every { ConfigIndexingActions.delete(request, any()) } returns response

        val deleteNotificationConfigAction = DeleteNotificationConfigAction(
            transportService, client, actionFilters, xContentRegistry
        )
        deleteNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get notification config action should call back action listener`() {
        val task = mock(Task::class.java)
        val request = mock(GetNotificationConfigRequest::class.java)
        val response = GetNotificationConfigResponse(
            mock(NotificationConfigSearchResult::class.java)
        )

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every { ConfigIndexingActions.get(request, any()) } returns response

        val getNotificationConfigAction = GetNotificationConfigAction(
            transportService, client, actionFilters, xContentRegistry
        )
        getNotificationConfigAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get notification event action should call back action listener`() {
        val task = mock(Task::class.java)
        val request = mock(GetNotificationEventRequest::class.java)
        val response = GetNotificationEventResponse(
            mock(NotificationEventSearchResult::class.java)
        )

        // Mock singleton's method by mockk framework
        mockkObject(EventIndexingActions)
        every { EventIndexingActions.get(request, any()) } returns response

        val getNotificationEventAction = GetNotificationEventAction(
            transportService, client, actionFilters, xContentRegistry
        )
        getNotificationEventAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get plugin features action should call back action listener`() {
        val allowedConfigTypes = listOf("type1")
        val pluginFeatures = mapOf(Pair("FeatureKey1", "Feature1"))
        val task = mock(Task::class.java)
        val request = mock(GetPluginFeaturesRequest::class.java)
        val response = GetPluginFeaturesResponse(allowedConfigTypes, pluginFeatures)

        // Mock singleton's method by mockk framework
        mockkObject(NotificationSpi)
        every { NotificationSpi.getAllowedConfigTypes() } returns allowedConfigTypes
        every { NotificationSpi.getPluginFeatures() } returns pluginFeatures

        val getPluginFeaturesAction = GetPluginFeaturesAction(
            transportService, client, actionFilters, xContentRegistry
        )
        getPluginFeaturesAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Get feature channel list action should call back action listener`() {
        val task = mock(Task::class.java)
        val request = mock(GetFeatureChannelListRequest::class.java)
        val response = GetFeatureChannelListResponse(mock(FeatureChannelList::class.java))

        // Mock singleton's method by mockk framework
        mockkObject(ConfigIndexingActions)
        every { ConfigIndexingActions.getFeatureChannelList(request, any()) } returns response

        val getFeatureChannelListAction = GetFeatureChannelListAction(
            transportService, client, actionFilters, xContentRegistry
        )
        getFeatureChannelListAction.execute(task, request, AssertionListener(response))
    }

    @Test
    fun `Send notification action should call back action listener`() {
        val notificationId = "notification-1"
        val task = mock(Task::class.java)
        val request = mock(SendNotificationRequest::class.java)
        val response = SendNotificationResponse(notificationId)

        // Mock singleton's method by mockk framework
        mockkObject(SendMessageActionHelper)
        every { SendMessageActionHelper.executeRequest(request) } returns response

        val sendNotificationAction = SendNotificationAction(
            transportService, client, actionFilters, xContentRegistry
        )
        sendNotificationAction.execute(task, request, AssertionListener(response))
    }

    /**
     * This listener class is to assert on response rather than verify it called.
     * The reason why this is required is because it is harder to do the latter
     * (verify listener being called once) due to CoroutineScope used in execute()
     */
    private class AssertionListener<Response: BaseResponse>(
        val expected: Response
    ): ActionListener<Response> {

        override fun onResponse(actual: Response?) {
            assertEquals(expected, actual)
        }

        override fun onFailure(error: Exception?) {
            fail("Unexpected error happened", error)
        }
    }
}
