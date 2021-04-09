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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class EmailTests {

    private fun checkValidEmailAddress(emailAddress: String) {
        assertDoesNotThrow("should accept $emailAddress") {
            Email("sampleId", listOf(emailAddress), listOf())
        }
    }

    private fun checkInvalidEmailAddress(emailAddress: String) {
        assertThrows<IllegalArgumentException>("Should throw an Exception for invalid email $emailAddress") {
            Email("sampleId", listOf(emailAddress), listOf())
        }
    }

    @Test
    fun `Email should accept valid email address`() {
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
    fun `Email should throw exception for invalid email address`() {
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
    fun `Email serialize and deserialize transport object should be equal`() {
        val sampleEmail = Email(
            "sampleAccountId",
            listOf("email1@email.com", "email2@email.com"),
            listOf("sample_group_id_1", "sample_group_id_2")
        )
        val recreatedObject = recreateObject(sampleEmail) { Email(it) }
        assertEquals(sampleEmail, recreatedObject)
    }

    @Test
    fun `Email serialize and deserialize using json object should be equal`() {
        val sampleEmail = Email(
            "sampleAccountId",
            listOf("email1@email.com", "email2@email.com"),
            listOf("sample_group_id_1", "sample_group_id_2")
        )
        val jsonString = getJsonString(sampleEmail)
        val recreatedObject = createObjectFromJsonString(jsonString) { Email.parse(it) }
        assertEquals(sampleEmail, recreatedObject)
    }

    @Test
    fun `Email should deserialize json object using parser`() {
        val sampleEmail = Email(
            "sampleAccountId",
            listOf("email1@email.com", "email2@email.com"),
            listOf("sample_group_id_1", "sample_group_id_2")
        )
        val jsonString = """
            {
                "emailAccountID":"${sampleEmail.emailAccountID}",
                "defaultRecipients":[
                    "${sampleEmail.defaultRecipients[0]}",
                    "${sampleEmail.defaultRecipients[1]}"
                ],
                "defaultEmailGroupIds":[
                    "${sampleEmail.defaultEmailGroupIds[0]}",
                    "${sampleEmail.defaultEmailGroupIds[1]}"
                ]
             }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Email.parse(it) }
        assertEquals(sampleEmail, recreatedObject)
    }

    @Test
    fun `Email should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Email.parse(it) }
        }
    }

    @Test
    fun `Email should throw exception when emailAccountID is replaced with emailAccountID2 in json object`() {
        val sampleEmail = Email(
            "sampleAccountId",
            listOf("email1@email.com", "email2@email.com"),
            listOf("sample_group_id_1", "sample_group_id_2")
        )
        val jsonString = """
            {
                "emailAccountID2":"${sampleEmail.emailAccountID}",
                "defaultRecipients":[
                    "${sampleEmail.defaultRecipients[0]}",
                    "${sampleEmail.defaultRecipients[1]}"
                ],
                "defaultEmailGroupIds":[
                    "${sampleEmail.defaultEmailGroupIds[0]}",
                    "${sampleEmail.defaultEmailGroupIds[1]}"
                ]
             }"
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Email.parse(it) }
        }
    }

    @Test
    fun `Email should accept without defaultRecipients and defaultEmailGroupIds in json object`() {
        val sampleEmail = Email("sampleAccountId", listOf(), listOf())
        val jsonString = """
            {
                "emailAccountID":"${sampleEmail.emailAccountID}"
            }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Email.parse(it) }
        assertEquals(sampleEmail, recreatedObject)
    }

    @Test
    fun `Email should safely ignore extra field in json object`() {
        val sampleEmail = Email("sampleAccountId", listOf(), listOf())
        val jsonString = """
            {
                "emailAccountID":"${sampleEmail.emailAccountID}",
                "defaultRecipients2":[
                    "email1@email.com",
                    "email2@email.com"
                ],
                "defaultEmailGroupIds2":[
                    "sample_group_id_1"
                ],
                "another":"field"
            }"
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { Email.parse(it) }
        assertEquals(sampleEmail, recreatedObject)
    }
}
