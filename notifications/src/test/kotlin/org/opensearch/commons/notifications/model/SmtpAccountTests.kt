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

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.common.settings.SecureString
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class SmtpAccountTests {

    @Test
    fun `SmtpAccount serialize and deserialize transport object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val recreatedObject = recreateObject(sampleSmtpAccount) { SmtpAccount(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount serialize and deserialize transport object should be equal with credentials`() {
        val sampleSmtpAccount = SmtpAccount(
            "domain.com",
            1234, SmtpAccount.MethodType.StartTls,
            "from@domain.com",
            SecureString("username".toCharArray()),
            SecureString("password".toCharArray())
        )
        val recreatedObject = recreateObject(sampleSmtpAccount) { SmtpAccount(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount serialize and deserialize using json object should be equal`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount serialize and deserialize using json object should be equal with credentials`() {
        val sampleSmtpAccount = SmtpAccount(
            "domain.com",
            1234, SmtpAccount.MethodType.StartTls,
            "from@domain.com",
            SecureString("username".toCharArray()),
            SecureString("password".toCharArray())
        )
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount should deserialize json object using parser`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"Ssl",
            "from_address":"from@domain.com"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount should deserialize json object using parser with credentials`() {
        val sampleSmtpAccount = SmtpAccount(
            "domain.com",
            1234, SmtpAccount.MethodType.StartTls,
            "from@domain.com",
            SecureString("given_username".toCharArray()),
            SecureString("given_password".toCharArray())
        )
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"StartTls",
            "from_address":"from@domain.com",
            "username":"given_username",
            "password":"given_password"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }

    @Test
    fun `SmtpAccount should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        }
    }

    @Test
    fun `SmtpAccount should throw exception when email id is invalid`() {
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"Ssl",
            "from_address":".from@domain.com",
            "username":"given_username",
            "password":"given_password"
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        }
    }

    @Test
    fun `SmtpAccount should safely ignore extra field in json object`() {
        val sampleSmtpAccount = SmtpAccount(
            "domain.com",
            1234, SmtpAccount.MethodType.Ssl,
            "from@domain.com",
            SecureString("given_username".toCharArray()),
            SecureString("given_password".toCharArray())
        )
        val jsonString = """
        {
            "host":"domain.com",
            "port":"1234",
            "method":"Ssl",
            "from_address":"from@domain.com",
            "username":"given_username",
            "password":"given_password",
            "extra_field_1":"extra value 1",
            "extra_field_2":"extra value 2"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SmtpAccount.parse(it) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }
}
