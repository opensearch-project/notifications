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
package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.notifications.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.notifications.getJsonString
import com.amazon.opendistroforelasticsearch.notifications.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.elasticsearch.test.ESTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class EmailGroupTests : ESTestCase() {

    private fun checkValidEmailAddress(emailAddress: String) {
        assertDoesNotThrow("should accept $emailAddress") {
            EmailGroup(listOf(emailAddress))
        }
    }

    private fun checkInvalidEmailAddress(emailAddress: String) {
        assertThrows<IllegalArgumentException>("Should throw an Exception for invalid email $emailAddress") {
            EmailGroup(listOf(emailAddress))
        }
    }

    @Test
    fun `EmailGroup should accept valid email address`() {
        checkValidEmailAddress("email1234@email.com")
        checkValidEmailAddress("email+1234@email.com")
        checkValidEmailAddress("email-1234@email.com")
        checkValidEmailAddress("email_1234@email.com")
        checkValidEmailAddress("email.1234@email.com")
        checkValidEmailAddress("e.ma_il-1+2@test-email-domain.co.uk")
        checkValidEmailAddress("email-.+_=#|@domain.com")
        checkValidEmailAddress("e@mail.com")
    }

    @Test
    fun `EmailGroup should throw exception for invalid email address`() {
        checkInvalidEmailAddress("email")
        checkInvalidEmailAddress("email@")
        checkInvalidEmailAddress("email@1234@email.com")
        checkInvalidEmailAddress(".email@email.com")
        checkInvalidEmailAddress("email.@email.com")
        checkInvalidEmailAddress("email..1234@email.com")
        checkInvalidEmailAddress("email@email..com")
        checkInvalidEmailAddress("email@.com")
        checkInvalidEmailAddress("email@email.com.")
        checkInvalidEmailAddress("email@.email.com")
        checkInvalidEmailAddress("email@email.com-")
        checkInvalidEmailAddress("email@email_domain.com")
    }

    @Test
    fun `EmailGroup serialize and deserialize transport object should be equal`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val recreatedObject = recreateObject(sampleEmailGroup) { EmailGroup(it) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `EmailGroup serialize and deserialize using json object should be equal`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = getJsonString(sampleEmailGroup)
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailGroup.parse(it) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `EmailGroup should deserialize json object using parser`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = """
            {
                "recipients":[
                    "${sampleEmailGroup.recipients[0]}",
                    "${sampleEmailGroup.recipients[1]}"
                ]
             }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailGroup.parse(it) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `EmailGroup should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { EmailGroup.parse(it) }
        }
    }

    @Test
    fun `EmailGroup should throw exception when recipients is replaced with recipients2 in json object`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = """
            {
                "recipients2":[
                    "${sampleEmailGroup.recipients[0]}",
                    "${sampleEmailGroup.recipients[1]}"
                ]
             }"
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { EmailGroup.parse(it) }
        }
    }

    @Test
    fun `EmailGroup should safely ignore extra field in json object`() {
        val sampleEmailGroup = EmailGroup(listOf("email@email.com"))
        val jsonString = "{\"recipients\":[\"${sampleEmailGroup.recipients[0]}\"], \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { EmailGroup.parse(it) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }
}
