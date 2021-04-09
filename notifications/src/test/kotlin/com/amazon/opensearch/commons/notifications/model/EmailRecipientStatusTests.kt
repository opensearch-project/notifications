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

package com.amazon.opensearch.commons.notifications.model

import com.amazon.opensearch.commons.utils.createObjectFromJsonString
import com.amazon.opensearch.commons.utils.getJsonString
import com.amazon.opensearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EmailRecipientStatusTests {
    @Test
    fun `EmailRecipientStatus serialize and deserialize should be equal`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("404", "invalid recipient")
        )
        val recreatedObject = recreateObject(sampleEmailRecipientStatus) { EmailRecipientStatus(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }

    @Test
    fun `EmailRecipientStatus serialize and deserialize using json should be equal`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("404", "invalid recipient")
        )
        val jsonString = getJsonString(sampleEmailRecipientStatus)
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }

    @Test
    fun `EmailRecipientStatus should throw exception for invalid recipient`() {
        assertThrows<IllegalArgumentException>("Should throw an Exception for invalid recipient Slack") {
            EmailRecipientStatus("slack", DeliveryStatus("404", "invalid recipient"))
        }
    }

    @Test
    fun `EmailRecipientStatus should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        }
    }

    @Test
    fun `EmailRecipientStatus should safely ignore extra field in json object`() {
        val sampleEmailRecipientStatus = EmailRecipientStatus(
            "sample@email.com",
            DeliveryStatus("200", "Success")
        )
        val jsonString = """
        {
            "recipient": "sample@email.com",
            "deliveryStatus": {
                "statusCode": "200",
                "statusText": "Success"
            },
            "extra": "field"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailRecipientStatus.parse(it) }
        assertEquals(sampleEmailRecipientStatus, recreatedObject)
    }
}
