/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder

/**
 * Response for the `POST /_plugins/_notifications/configs/_reencrypt` endpoint.
 *
 * Fields:
 * - **migrated** – configs successfully re-encrypted with the current active key.
 * - **skipped**  – configs that were already encrypted with the active key (no action needed).
 * - **failed**   – configs that could not be re-encrypted (check plugin logs for details).
 * - **remaining** – number of configs that still need re-encryption (equals [failed] after each run).
 *
 * Callers should re-run the endpoint until `remaining == 0`.
 */
class ReencryptNotificationConfigsResponse(
    val migrated: Int,
    val skipped: Int,
    val failed: Int,
    val remaining: Int
) : ActionResponse(), ToXContentObject {

    companion object {
        private const val MIGRATED_TAG = "migrated"
        private const val SKIPPED_TAG = "skipped"
        private const val FAILED_TAG = "failed"
        private const val REMAINING_TAG = "remaining"
    }

    constructor(sin: StreamInput) : this(
        migrated = sin.readInt(),
        skipped = sin.readInt(),
        failed = sin.readInt(),
        remaining = sin.readInt()
    )

    override fun writeTo(out: StreamOutput) {
        out.writeInt(migrated)
        out.writeInt(skipped)
        out.writeInt(failed)
        out.writeInt(remaining)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(MIGRATED_TAG, migrated)
            .field(SKIPPED_TAG, skipped)
            .field(FAILED_TAG, failed)
            .field(REMAINING_TAG, remaining)
            .endObject()
    }
}
