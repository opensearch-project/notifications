/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.security.spi.resources.client.ResourceSharingClient
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ResourceSharingTests {

    @BeforeEach
    fun setup() {
        ResourceSharingClientAccessor.clear()
    }

    @Test
    fun `get client returns null when not set`() {
        assertNull(ResourceSharingClientAccessor.getResourceSharingClient())
    }

    @Test
    fun `set and get client`() {
        val mockClient = mock(ResourceSharingClient::class.java)
        ResourceSharingClientAccessor.setResourceSharingClient(mockClient)
        assertSame(mockClient, ResourceSharingClientAccessor.getResourceSharingClient())
    }

    @Test
    fun `clear resets client to null`() {
        val mockClient = mock(ResourceSharingClient::class.java)
        ResourceSharingClientAccessor.setResourceSharingClient(mockClient)
        ResourceSharingClientAccessor.clear()
        assertNull(ResourceSharingClientAccessor.getResourceSharingClient())
    }

    @Test
    fun `extension returns one provider`() {
        val extension = NotificationsResourceSharingExtension()
        val providers = extension.getResourceProviders()
        assertEquals(1, providers.size)
    }

    @Test
    fun `provider has correct type and index`() {
        val extension = NotificationsResourceSharingExtension()
        val provider = extension.getResourceProviders().first()
        assertEquals("notification_config", provider.resourceType())
        assertEquals(NotificationConfigIndex.INDEX_NAME, provider.resourceIndexName())
    }

    @Test
    fun `assignResourceSharingClient sets client`() {
        val extension = NotificationsResourceSharingExtension()
        val mockClient = mock(ResourceSharingClient::class.java)
        extension.assignResourceSharingClient(mockClient)
        assertSame(mockClient, ResourceSharingClientAccessor.getResourceSharingClient())
    }
}
