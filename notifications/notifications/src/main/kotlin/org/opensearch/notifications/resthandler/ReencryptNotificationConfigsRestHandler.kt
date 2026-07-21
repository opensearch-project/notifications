/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.resthandler

import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.notifications.action.ReencryptNotificationConfigsAction
import org.opensearch.notifications.model.ReencryptNotificationConfigsRequest
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.action.RestToXContentListener
import org.opensearch.transport.client.node.NodeClient

/**
 * REST handler for `POST /_plugins/_notifications/configs/_reencrypt`.
 *
 * Re-encrypts all stored notification channel-configuration secrets with the
 * current active field-encryption key. Used as part of the key-rotation runbook
 * (Phase 3) to migrate existing ciphertexts produced by the previous key.
 *
 * The endpoint is idempotent: records already encrypted by the active key are
 * skipped automatically. Repeat the call until the response shows `remaining == 0`.
 */
internal class ReencryptNotificationConfigsRestHandler : PluginBaseHandler() {

    companion object {
        private val log by logger(ReencryptNotificationConfigsRestHandler::class.java)
        private const val REQUEST_URL = "$PLUGIN_BASE_URI/configs/_reencrypt"
    }

    override fun getName(): String = "notifications_reencrypt_configs"

    override fun routes(): List<Route> = listOf(
        /**
         * Re-encrypt all notification config secrets with the current active key.
         * Request URL: POST [REQUEST_URL]
         * Response body: { "migrated": N, "skipped": N, "failed": N, "remaining": N }
         */
        Route(POST, REQUEST_URL)
    )

    override fun responseParams(): Set<String> = emptySet()

    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        log.info("REST $REQUEST_URL called")
        return RestChannelConsumer { channel ->
            client.execute(
                ReencryptNotificationConfigsAction.ACTION_TYPE,
                ReencryptNotificationConfigsRequest(),
                RestToXContentListener(channel)
            )
        }
    }
}
