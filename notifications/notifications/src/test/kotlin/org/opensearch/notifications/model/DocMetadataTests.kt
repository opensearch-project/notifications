/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.notifications.createObjectFromJsonString
import org.opensearch.notifications.getJsonString
import java.time.Instant

internal class DocMetadataTests {

    @Test
    fun `DocMetadata serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            listOf("br1", "br2", "br3")
        )
        val jsonString = getJsonString(metadata)
        val recreatedObject = createObjectFromJsonString(jsonString) { DocMetadata.parse(it) }
        assertEquals(metadata, recreatedObject)
    }

    @Test
    fun `DocMetadata should take empty list when access field is absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            listOf()
        )
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DocMetadata.parse(it) }
        assertEquals(metadata, recreatedObject)
    }

    @Test
    fun `DocMetadata should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            listOf("br1", "br2", "br3")
        )
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "access":["br1", "br2", "br3"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DocMetadata.parse(it) }
        assertEquals(metadata, recreatedObject)
    }

    @Test
    fun `DocMetadata should throw exception if last_updated_time_ms is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "access":["br1", "br2", "br3"]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }

    @Test
    fun `DocMetadata should throw exception if created_time_ms is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "access":["br1", "br2", "br3"]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }
}
