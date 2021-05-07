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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.notifications.channel

import org.opensearch.notifications.NotificationsRestTestCase
import org.opensearch.notifications.getStatusCode
import org.opensearch.notifications.getStatusText
import org.opensearch.notifications.jsonify
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.verifyResponse
import org.opensearch.rest.RestStatus

internal class SmtpChannelIT : NotificationsRestTestCase() {
    private val refTag = "sample ref name"
    private val title = "sample title"
    private val textDescription = "Description for notification in text"
    private val htmlDescription = "Description for notification in json encode html format"
    private val attachment = jsonify(
        """
        {
          "file_name": "odfe.data",
          "file_encoding": "base64",
          "file_content_type": "application/octet-stream",
          "file_data": "VGVzdCBtZXNzYWdlCgo="
        }
        """.trimIndent()
    )

    fun `test send email to one recipient over Smtp server`() {
        val recipients = listOf("mailto:test@localhost")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)
        verifyResponse(response, refTag, recipients)
    }

    fun `test send email to multiple recipient over Smtp server`() {
        val recipients = listOf("mailto:test1@localhost", "mailto:test2@abc.com", "mailto:test3@123.com")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)
        verifyResponse(response, refTag, recipients)
    }

    fun `test send email with unconfigured address`() {
        setFromAddress(PluginSettings.UNCONFIGURED_EMAIL_ADDRESS)
        val recipients = listOf("mailto:test@localhost")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)

        val statusCode = getStatusCode(response)
        assertEquals(RestStatus.NOT_IMPLEMENTED.status, statusCode)

        val statusText = getStatusText(response)
        assertEquals("Email from: address not configured", statusText)
        resetFromAddress()
    }
}
