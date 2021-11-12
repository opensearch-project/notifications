/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_ALERTING
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_INDEX_MANAGEMENT
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.utils.recreateObject
import org.opensearch.notifications.createObjectFromJsonString
import org.opensearch.notifications.getJsonString
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SendTestNotificationRequestTests {

    private fun assertSendTestRequestEquals(
        expected: SendTestNotificationRequest,
        actual: SendTestNotificationRequest
    ) {
        assertEquals(expected.feature, actual.feature)
        assertEquals(expected.configId, actual.configId)
    }

    @Test
    fun `Send test request serialize and deserialize transport object should be equal`() {
        val sendTestRequest = SendTestNotificationRequest(FEATURE_REPORTS, "configId")
        val recreatedObject = recreateObject(sendTestRequest) { SendTestNotificationRequest(it) }
        assertSendTestRequestEquals(sendTestRequest, recreatedObject)
    }

    @Test
    fun `Send test request serialize and deserialize using json object should be equal`() {
        val sendTestRequest = SendTestNotificationRequest(FEATURE_INDEX_MANAGEMENT, "configId")
        val jsonString = getJsonString(sendTestRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        assertSendTestRequestEquals(sendTestRequest, recreatedObject)
    }

    @Test
    fun `Send test request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send test request should return null for valid request`() {
        val sendTestRequest = SendTestNotificationRequest(FEATURE_INDEX_MANAGEMENT, "configId")
        assertNull(sendTestRequest.validate())
    }

    @Test
    fun `Send test request should return exception when configId is empty for validate`() {
        val sendTestRequest = SendTestNotificationRequest(FEATURE_INDEX_MANAGEMENT, "")
        assertNotNull(sendTestRequest.validate())
    }

    @Test
    fun `Send test request should safely ignore extra field in json object`() {
        val sendTestRequest = SendTestNotificationRequest(FEATURE_ALERTING, "configId")
        val jsonString = """
        {
            "feature":"${sendTestRequest.feature}",
            "config_id":"${sendTestRequest.configId}",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        assertSendTestRequestEquals(sendTestRequest, recreatedObject)
    }

    @Test
    fun `Send test request should throw exception if feature field is absent in json object`() {
        val jsonString = """
        {
            "config_id":"configId"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        }
    }

    @Test
    fun `Send test request should throw exception if configId field is absent in json object`() {
        val jsonString = """
        {
            "feature":"feature"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        }
    }
}
