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
package org.opensearch.commons.notifications.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class EventSourceTests {

    @Test
    fun `Event source serialize and deserialize should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            severity = SeverityType.INFO
        )
        val recreatedObject = recreateObject(sampleEventSource) { EventSource(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source serialize and deserialize using json should be equal`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            severity = SeverityType.INFO
        )

        val jsonString = getJsonString(sampleEventSource)
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source should safely ignore extra field in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val jsonString = """
        { 
            "title":"title",
            "reference_id":"reference_id",
            "feature":"alerting",
            "severity":"info",
            "tags":["tag1", "tag2"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source should safely ignore unknown feature type in json object`() {
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.NONE,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val jsonString = """
        {
            "title":"title",
            "reference_id":"reference_id",
            "feature": "NewFeature",
            "severity":"info",
            "tags":["tag1", "tag2"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EventSource.parse(it) }
        assertEquals(sampleEventSource, recreatedObject)
    }

    @Test
    fun `Event source throw exception if name is empty`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            EventSource(
                "",
                "reference_id",
                Feature.ALERTING,
                tags = listOf("tag1", "tag2"),
                severity = SeverityType.INFO
            )
        }
    }
}
