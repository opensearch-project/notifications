/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.common.inject.Inject
import org.opensearch.commons.authuser.User
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.notifications.model.ReencryptNotificationConfigsRequest
import org.opensearch.notifications.model.ReencryptNotificationConfigsResponse
import org.opensearch.notifications.util.ConfigEncryptionTransformer
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client

/**
 * Transport action for `POST /_plugins/_notifications/configs/_reencrypt`.
 *
 * Pages through every stored notification-channel configuration and re-encrypts
 * any field whose ciphertext was produced by the previous key (i.e. the key that
 * is present only during a rotation window). Records already encrypted with the
 * current active key are skipped.
 */
internal class ReencryptNotificationConfigsAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters
) : PluginBaseAction<ReencryptNotificationConfigsRequest, ReencryptNotificationConfigsResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::ReencryptNotificationConfigsRequest
) {
    companion object {
        internal const val NAME = "cluster:admin/opensearch/notifications/configs/reencrypt"
        internal val ACTION_TYPE = ActionType(NAME, ::ReencryptNotificationConfigsResponse)

        private const val PAGE_SIZE = 100
        private val log by logger(ReencryptNotificationConfigsAction::class.java)
    }

    override suspend fun executeRequest(
        request: ReencryptNotificationConfigsRequest,
        user: User?
    ): ReencryptNotificationConfigsResponse {
        log.info("$LOG_PREFIX:ReencryptNotificationConfigs starting")

        var migrated = 0
        var skipped = 0
        var failed = 0
        var from = 0

        while (true) {
            val (docs, total) = NotificationConfigIndex.getAllRawNotificationConfigs(from, PAGE_SIZE)
            if (docs.isEmpty()) break

            for (docInfo in docs) {
                val id = docInfo.docInfo.id ?: continue
                val rawConfig = docInfo.configDoc.config
                try {
                    if (ConfigEncryptionTransformer.needsReencryption(rawConfig)) {
                        // Decrypt using the current service (active key + previous key fallback),
                        // then write back — updateNotificationConfig will re-encrypt with the active key.
                        val decryptedConfig = ConfigEncryptionTransformer.decryptConfig(rawConfig)
                        val updatedDoc = docInfo.configDoc.copy(config = decryptedConfig)
                        if (NotificationConfigIndex.updateNotificationConfig(id, updatedDoc)) {
                            log.debug("$LOG_PREFIX:ReencryptNotificationConfigs migrated config $id")
                            migrated++
                        } else {
                            log.warn("$LOG_PREFIX:ReencryptNotificationConfigs update returned false for $id")
                            failed++
                        }
                    } else {
                        skipped++
                    }
                } catch (e: Exception) {
                    log.error("$LOG_PREFIX:ReencryptNotificationConfigs failed for config $id", e)
                    failed++
                }
            }

            from += docs.size
            if (from >= total) break
        }

        log.info("$LOG_PREFIX:ReencryptNotificationConfigs done — migrated=$migrated skipped=$skipped failed=$failed")
        return ReencryptNotificationConfigsResponse(
            migrated = migrated,
            skipped = skipped,
            failed = failed,
            remaining = failed
        )
    }
}
