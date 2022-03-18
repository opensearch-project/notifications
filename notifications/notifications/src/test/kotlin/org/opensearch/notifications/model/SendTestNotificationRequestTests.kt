/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        assertEquals(expected.configId, actual.configId)
    }

    @Test
    fun `Send test request serialize and deserialize transport object should be equal`() {
        val sendTestRequest = SendTestNotificationRequest("configId")
        val recreatedObject = recreateObject(sendTestRequest) { SendTestNotificationRequest(it) }
        assertSendTestRequestEquals(sendTestRequest, recreatedObject)
    }

    @Test
    fun `Send test request serialize and deserialize using json object should be equal`() {
        val sendTestRequest = SendTestNotificationRequest("configId")
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
        val sendTestRequest = SendTestNotificationRequest("configId")
        assertNull(sendTestRequest.validate())
    }

    @Test
    fun `Send test request should return exception when configId is empty for validate`() {
        val sendTestRequest = SendTestNotificationRequest("")
        assertNotNull(sendTestRequest.validate())
    }

    @Test
    fun `Send test request should safely ignore extra field in json object`() {
        val sendTestRequest = SendTestNotificationRequest("configId")
        val jsonString = """
        {
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
    fun `Send test request should throw exception if configId field is absent in json object`() {
        val jsonString = """
        {
            "extra_field_1":"extra value 1"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SendTestNotificationRequest.parse(it) }
        }
    }
}
