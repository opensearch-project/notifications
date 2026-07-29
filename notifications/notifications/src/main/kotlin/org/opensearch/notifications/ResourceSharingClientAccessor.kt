/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications

import org.opensearch.security.spi.resources.client.ResourceSharingClient

/**
 * Accessor for resource sharing client
 */
object ResourceSharingClientAccessor {

    @Volatile
    private var client: ResourceSharingClient? = null

    @JvmStatic
    fun setResourceSharingClient(client: ResourceSharingClient?) {
        this.client = client
    }

    @JvmStatic
    fun getResourceSharingClient(): ResourceSharingClient? = client

    @JvmStatic
    fun clear() {
        client = null
    }
}
