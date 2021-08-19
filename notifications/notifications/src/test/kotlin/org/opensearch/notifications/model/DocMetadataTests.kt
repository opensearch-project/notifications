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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
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
            "tenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val jsonString = getJsonString(metadata)
        val recreatedObject = createObjectFromJsonString(jsonString) { DocMetadata.parse(it) }
        assertEquals(metadata, recreatedObject)
    }

    @Test
    fun `DocMetadata should take default tenant when field is absent in json object`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            "", // Default tenant
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
        }
        """.trimIndent()
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
            "selectedTenant",
            listOf()
        )
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant"
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
            "selectedTenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val jsonString = """
        {
            "last_updated_time_ms":"${lastUpdatedTimeMs.toEpochMilli()}",
            "created_time_ms":"${createdTimeMs.toEpochMilli()}",
            "tenant":"selectedTenant",
            "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"],
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
            "tenant":"selectedTenant",
            "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
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
            "tenant":"selectedTenant",
            "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { NotificationConfigInfo.parse(it) }
        }
    }
}
