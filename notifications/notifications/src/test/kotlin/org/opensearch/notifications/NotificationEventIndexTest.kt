package org.opensearch.notifications.index

import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.opensearch.action.get.GetRequest
import org.opensearch.client.Client
import org.opensearch.commons.utils.logger
import org.opensearch.cluster.service.ClusterService
import org.opensearch.commons.notifications.model.NotificationEvent
import org.opensearch.commons.notifications.model.NotificationEventInfo
import org.opensearch.commons.notifications.model.SearchResults
import org.opensearch.notifications.settings.PluginSettings
import org.junit.jupiter.api.BeforeEach
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.any
import junit.framework.Assert.assertEquals
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.model.NotificationEventDocInfo




internal class NotificationEventIndexTest{

    @Mock
    private lateinit var client: Client
  
    private val INDEX_NAME = ".opensearch-notifications-event"
    
    @Mock
    private lateinit var clusterService: ClusterService

    @BeforeEach
    fun setUp() {
        NotificationEventIndex.initialize(client, clusterService)
    }

    @Test
    fun `index operation to get single event` () {
        val id = "index-1"
        val docInfo = DocInfo("index-1", 1, 1, 1)
        val eventDoc = mock(NotificationEventDoc::class.java, RETURNS_DEEP_STUBS)
        val expectedEventDocInfo = NotificationEventDocInfo(docInfo, eventDoc)

        val getRequest = GetRequest(INDEX_NAME).id(id)
        val mockActionFuture = client.get(getRequest)
        //whenever(NotificationEventIndex.client.get(any())).thenReturn(mockActionFuture)

        whenever(NotificationEventIndex.client.get(getRequest)).thenReturn(mockActionFuture)

        val actualEventDocInfo = NotificationEventIndex.getNotificationEvent(id)

        /*
        whenever(client.get(any())).thenReturn(mockFuture)
         */

       // val actualEventDocInfo = NotificationEventIndex.getNotificationEvent(id)

        assertEquals(expectedEventDocInfo, actualEventDocInfo)

    }

}
