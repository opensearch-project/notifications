/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.notifications.createObjectFromJsonString
import org.opensearch.notifications.getJsonString
import java.time.Instant

internal class NotificationConfigDocTests {

    @Test
    fun `Config doc serialize and deserialize using json config object should be equal`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            listOf("br1", "br2", "br3")
        )
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack
        )
        val configDoc = NotificationConfigDoc(metadata, config)
        val jsonString = getJsonString(configDoc)
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigDoc.parse(it) }
        assertEquals(configDoc, recreatedObject)
    }

    @Test
    fun `Config doc should safely ignore extra field in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            listOf("br1", "br2", "br3")
        )
        val sampleSlack = Slack("https://hooks.slack.com/services/sample_slack_url")
        val config = NotificationConfig(
            "name",
            "description",
            ConfigType.SLACK,
            configData = sampleSlack
        )
        val configDoc = NotificationConfigDoc(metadata, config)
        val jsonString = """
        {
            "metadata":{
                "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                "access":["br1", "br2", "br3"]
            },
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{"url":"https://hooks.slack.com/services/sample_slack_url"}
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { NotificationConfigDoc.parse(it) }
        assertEquals(configDoc, recreatedObject)
    }

    @Test
    fun `Config doc should throw exception if metadata is absent in json`() {
        val jsonString = """
        {
            "config":{
                "name":"name",
                "description":"description",
                "config_type":"slack",
                "is_enabled":true,
                "slack":{"url":"https://domain.com/sample_slack_url#1234567890"}
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigDoc.parse(it) }
        }
    }

    @Test
    fun `Config doc should throw exception if config is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "metadata":{
                "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
                "created_time_ms":"${createdTimeMs.toEpochMilli()}",
                "access":["br1", "br2", "br3"]
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigDoc.parse(it) }
        }
    }
}
