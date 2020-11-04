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

package com.amazon.opendistroforelasticsearch.notifications.channel

import com.amazon.opendistroforelasticsearch.notifications.NotificationsRestTestCase
import com.amazon.opendistroforelasticsearch.notifications.jsonify
import com.amazon.opendistroforelasticsearch.notifications.getStatusCode
import com.amazon.opendistroforelasticsearch.notifications.getStatusText
import org.elasticsearch.rest.RestStatus
import org.junit.After

class SesChannelIT : NotificationsRestTestCase() {
    private val refTag = "ref"
    private val title = "title"
    private val textDescription = "text"
    private val htmlDescription = "html"
    private val attachment = jsonify(
        """
        {
          "fileName": "odfe.data",
          "fileEncoding": "base64",
          "fileContentType": "application/octet-stream",
          "fileData": "VGVzdCBtZXNzYWdlCgo="
        }
        """.trimIndent())

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