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
package org.opensearch.notifications.index

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mock
import org.opensearch.action.ActionFuture
import org.opensearch.action.admin.indices.create.CreateIndexResponse
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.client.AdminClient
import org.opensearch.client.Client
import org.opensearch.client.IndicesAdminClient
import org.opensearch.cluster.ClusterState
import org.opensearch.cluster.routing.RoutingTable
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.common.util.concurrent.ThreadContext.StoredContext
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_ALERTING
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.DeliveryStatus
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.EventStatus
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.model.NotificationEventDocInfo
import org.opensearch.threadpool.ThreadPool
import java.time.Instant

internal class NotificationEventIndexTest {

    private lateinit var client: Client

    private val indexName = ".opensearch-notifications-event"

    private lateinit var clusterService: ClusterService

    @BeforeEach
    fun setUp() {
        client = mock(Client::class.java, "client")
        clusterService = mock(ClusterService::class.java, "clusterService")
        NotificationEventIndex.initialize(client, clusterService)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `index operation to get single event`() {
        // creating expected value
        val id = "index-1"
        val docInfo = DocInfo("index-1", 1, 1, 1)
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            FEATURE_ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val status = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(status))
        val eventDoc = NotificationEventDoc(metadata, sampleEvent)
        val expectedEventDocInfo = NotificationEventDocInfo(docInfo, eventDoc)

        // mocking the dependencies for isIndexExists function
        mockIsIndexExists()

        // creating a mock index response for client
        val mockActionGet = mock(CreateIndexResponse::class.java)
        mockCreateIndex(mockActionGet)
        whenever(mockActionGet.isAcknowledged).thenReturn(true)

        // creating a get request
        val getRequest = GetRequest(indexName).id(id)

        // mocking ActionFuture to create response for the get request
        val mockActionFuture: ActionFuture<GetResponse> = mock(ActionFuture::class.java) as ActionFuture<GetResponse>
        whenever(client.get(getRequest)).thenReturn(mockActionFuture)

        // mocking the get request of Secure Index client
        mockGetSecureIndexClient(mockActionFuture)

        // create mock response for actionGet
        val mockGetResponse = mock(GetResponse::class.java)
        whenever(mockActionFuture.actionGet(anyLong())).thenReturn(mockGetResponse)

        whenever(mockGetResponse.sourceAsString).thenReturn(true.toString())
        whenever(mockGetResponse.version).thenReturn(1)
        whenever(mockGetResponse.primaryTerm).thenReturn(1)
        whenever(mockGetResponse.seqNo).thenReturn(1)

        // mock object
        mockkObject(NotificationEventDoc)
        every { NotificationEventDoc.parse(any()) } returns eventDoc

        val actualEventDocInfo = NotificationEventIndex.getNotificationEvent(id)
        assertEquals(expectedEventDocInfo, actualEventDocInfo)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `NotificationEventIndex should safely return null if response source is null`() {
        val id = "index-1"
        // mocking the dependencies for isIndexExists function
        mockIsIndexExists()

        // creating a mock index response for client
        val mockActionGet = mock(CreateIndexResponse::class.java)
        mockCreateIndex(mockActionGet)
        whenever(mockActionGet.isAcknowledged).thenReturn(true)

        // creating a get request
        val getRequest = GetRequest(indexName).id(id)
        // mock ActionFuture to create response for the get request
        val mockActionFuture: ActionFuture<GetResponse> = mock(ActionFuture::class.java) as ActionFuture<GetResponse>
        whenever(client.get(getRequest)).thenReturn(mockActionFuture)

        // mocking the get request of Secure Index client
        mockGetSecureIndexClient(mockActionFuture)

        // create mock response for actionGet
        val mockGetResponse = mock(GetResponse::class.java)
        whenever(mockActionFuture.actionGet(anyLong())).thenReturn(mockGetResponse)

        val actualEventDocInfo = NotificationEventIndex.getNotificationEvent(id)
        assertNull(actualEventDocInfo)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `NotificationEventIndex should throw exception if response isn't acknowledged`() {
        val id = "index-1"
        // mocking the dependencies for isIndexExists function
        mockIsIndexExists()

        // creating a mock index response for client
        val mockActionGet = mock(CreateIndexResponse::class.java)
        mockCreateIndex(mockActionGet)
        Assertions.assertThrows(IllegalStateException::class.java) {
            NotificationEventIndex.getNotificationEvent(id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `NotificationEventIndex should throw exception if index couldn't be created`() {
        val id = "index-1"
        // mocking the dependencies for isIndexExists function
        mockIsIndexExists()
        Assertions.assertThrows(Exception::class.java) {
            NotificationEventIndex.getNotificationEvent(id)
        }
    }

    private fun mockIsIndexExists() {
        val clusterState = mock(ClusterState::class.java)
        whenever(clusterService.state()).thenReturn(clusterState)

        val mockRoutingTable = mock(RoutingTable::class.java)
        val mockHasIndex = mockRoutingTable.hasIndex(indexName)
        whenever(clusterState.routingTable).thenReturn(mockRoutingTable)
        whenever(mockRoutingTable.hasIndex(indexName)).thenReturn(mockHasIndex)
    }

    @Suppress("UNCHECKED_CAST")
    private fun mockCreateIndex(mockActionGet: CreateIndexResponse) {
        val admin = mock(AdminClient::class.java)
        val indices = mock(IndicesAdminClient::class.java)
        val mockCreateClient: ActionFuture<CreateIndexResponse> =
            mock(ActionFuture::class.java) as ActionFuture<CreateIndexResponse>

        whenever(client.admin()).thenReturn(admin)
        whenever(admin.indices()).thenReturn(indices)
        whenever(indices.create(any())).thenReturn(mockCreateClient)
        whenever(mockCreateClient.actionGet(anyLong())).thenReturn(mockActionGet)
    }

    private fun mockGetSecureIndexClient(mockActionFuture: ActionFuture<GetResponse>) {
        val mockThreadPool = mock(ThreadPool::class.java)
        val mockThreadContext = mock(ThreadContext::class.java)
        val mockStashContext = mock(StoredContext::class.java)

        whenever(client.threadPool()).thenReturn(mockThreadPool)
        whenever(mockThreadPool.threadContext).thenReturn(mockThreadContext)
        whenever(mockThreadContext.stashContext()).thenReturn(mockStashContext)
        whenever(client.get(any())).thenReturn(mockActionFuture)
    }
}
