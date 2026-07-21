/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications

import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.security.spi.resources.ResourceProvider
import org.opensearch.security.spi.resources.ResourceSharingExtension
import org.opensearch.security.spi.resources.client.ResourceSharingClient

class NotificationsResourceSharingExtension : ResourceSharingExtension {

    companion object {
        const val RESOURCE_TYPE = "notification_config"
    }

    override fun getResourceProviders(): Set<ResourceProvider> {
        return setOf(
            object : ResourceProvider {
                override fun resourceType(): String = RESOURCE_TYPE
                override fun resourceIndexName(): String = NotificationConfigIndex.INDEX_NAME
            }
        )
    }

    override fun assignResourceSharingClient(client: ResourceSharingClient?) {
        ResourceSharingClientAccessor.setResourceSharingClient(client)
    }
}
