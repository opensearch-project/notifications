/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.getJsonString

internal class ReencryptNotificationConfigsResponseTests {

    private fun response(migrated: Int = 0, skipped: Int = 0, failed: Int = 0, remaining: Int = 0) =
        ReencryptNotificationConfigsResponse(migrated, skipped, failed, remaining)

    @Nested
    inner class StreamSerialization {

        @Test
        fun `round-trips all fields over transport stream`() {
            val original = response(migrated = 7, skipped = 3, failed = 2, remaining = 2)
            val recreated = recreateObject(original) { ReencryptNotificationConfigsResponse(it) }

            assertEquals(original.migrated, recreated.migrated)
            assertEquals(original.skipped, recreated.skipped)
            assertEquals(original.failed, recreated.failed)
            assertEquals(original.remaining, recreated.remaining)
        }

        @Test
        fun `round-trips all-zero response`() {
            val original = response()
            val recreated = recreateObject(original) { ReencryptNotificationConfigsResponse(it) }

            assertEquals(0, recreated.migrated)
            assertEquals(0, recreated.skipped)
            assertEquals(0, recreated.failed)
            assertEquals(0, recreated.remaining)
        }
    }

    @Nested
    inner class XContentSerialization {

        @Test
        fun `produces JSON with migrated field`() {
            val json = getJsonString(response(migrated = 5))
            assert(json.contains("\"migrated\":5")) { "Expected migrated:5 in: $json" }
        }

        @Test
        fun `produces JSON with skipped field`() {
            val json = getJsonString(response(skipped = 10))
            assert(json.contains("\"skipped\":10")) { "Expected skipped:10 in: $json" }
        }

        @Test
        fun `produces JSON with failed field`() {
            val json = getJsonString(response(failed = 3))
            assert(json.contains("\"failed\":3")) { "Expected failed:3 in: $json" }
        }

        @Test
        fun `produces JSON with remaining field`() {
            val json = getJsonString(response(remaining = 3))
            assert(json.contains("\"remaining\":3")) { "Expected remaining:3 in: $json" }
        }

        @Test
        fun `remaining equals failed in a typical rotation response`() {
            val r = response(migrated = 8, skipped = 12, failed = 2, remaining = 2)
            assertEquals(r.failed, r.remaining)
        }

        @Test
        fun `all fields are present in JSON output`() {
            val json = getJsonString(response(migrated = 1, skipped = 2, failed = 3, remaining = 3))
            listOf("migrated", "skipped", "failed", "remaining").forEach { field ->
                assert(json.contains("\"$field\"")) { "Expected field $field in: $json" }
            }
        }
    }
}
