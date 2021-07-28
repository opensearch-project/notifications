package org.opensearch.notifications.index

//import org.opensearch.commons.notifications.NotificationsPluginInterface.getNotificationEvent
import org.opensearch.notifications.index.NotificationEventIndex.getNotificationEvent
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import io.mockk.every
import io.mockk.mockkObject
import org.opensearch.action.get.GetRequest
import kotlin.text.*
import org.opensearch.notifications.index.NotificationEventIndex
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.settings.PluginSettings


@ExtendWith(MockitoExtension::class)
internal class NotificationEventIndexTest {

    //private val NotificationEventIndex = NotificationEventIndex()

    @Mock
    private lateinit var client: Client

    @Mock
    private const val INDEX_NAME = ".opensearch-notifications-event"

    //@Mock
    //private val log by logger(NotificationEventIndex::class.java)

    @Mock
    private const val MAPPING_FILE_NAME = "notifications-event-mapping.yml"

    @Mock
    private const val SETTINGS_FILE_NAME = "notifications-event-settings.yml"

    @Mock
    private const val MAPPING_TYPE = "_doc"

    @Mock
    private lateinit var clusterService: ClusterService

    @Test
    fun `index operation to get single event` () {

        val id = "index-1"
       // val NotificationEventIndex = mock(NotificationEventIndex::class.java)
        val getRequest = GetRequest(NotificationEventIndex.INDEX_NAME).id(id)
        val actionFuture = NotificationEventIndex.client.get(getRequest)
        //val PluginSettings = mock(PluginSettings::class.java)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val expected = NotificationEventIndex.parseNotificationEventDoc(id, response)


        //mockkObject(NotificationEventIndex)
        //every { NotificationEventIndex.parseNotificationEventDoc(id, response)} returns response

        val getNotificationEvent = getNotificationEvent(id)

    }
}
