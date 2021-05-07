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

import org.junit.After
import org.opensearch.notifications.NotificationsRestTestCase
import org.opensearch.notifications.getStatusCode
import org.opensearch.notifications.getStatusText
import org.opensearch.notifications.jsonify
import org.opensearch.rest.RestStatus

class SesChannelIT : NotificationsRestTestCase() {
    private val refTag = "ref"
    private val title = "title"
    private val textDescription = "text"
    private val htmlDescription = "html"
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

    override fun init() {
        setChannelType("ses")
    }

    @After
    fun reset() {
        resetChannelType()
    }

    fun `test send email over ses channel due to ses authorization failure`() {
        val recipients = listOf("mailto:test@localhost")
        val response = executeRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)

        val statusCode = getStatusCode(response)
        assertEquals(RestStatus.SERVICE_UNAVAILABLE.status, statusCode)

        val statusText = getStatusText(response)
        assertEquals("sendEmail Error, SES status:400:Optional[Bad Request]", statusText)
    }
}
