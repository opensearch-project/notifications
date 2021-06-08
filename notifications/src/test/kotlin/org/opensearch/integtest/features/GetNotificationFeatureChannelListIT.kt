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

package org.opensearch.integtest.features

import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class GetNotificationFeatureChannelListIT : PluginRestTestCase() {

    fun `test Get feature channel list should error for empty feature`() {
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/feature/channels/",
            "",
            RestStatus.BAD_REQUEST.status
        )
    }

    fun `test POST feature channel list should result in error`() {
        executeRequest(
            RestRequest.Method.POST.name,
            "$PLUGIN_BASE_URI/feature/channels/reports",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test PUT feature channel list should result in error`() {
        executeRequest(
            RestRequest.Method.PUT.name,
            "$PLUGIN_BASE_URI/feature/channels/alerting",
            "{\"feature\":\"reports\"}",
            RestStatus.METHOD_NOT_ALLOWED.status
        )
    }

    fun `test Get feature channel list should error for invalid feature`() {
        executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/feature/channels/new_feature",
            "",
            RestStatus.BAD_REQUEST.status
        )
    }
}
