/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.spi.integTest

import org.junit.After
import org.opensearch.notifications.spi.Notification
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.EmailDestination
import org.opensearch.rest.RestStatus
import org.opensearch.test.rest.OpenSearchRestTestCase
import org.springframework.integration.test.mail.TestMailServer

internal class SmtpEmailIT : OpenSearchRestTestCase() {

    private val smtpServer: TestMailServer.SmtpServer
    private val smtpPort = 10255 // use non-standard port > 1024 to avoid permission issue

    init {
        smtpServer = TestMailServer.smtp(smtpPort)
    }

    @After
    fun tearDownServer() {
        smtpServer.stop()
        smtpServer.resetServer()
    }

    fun `test send email to one recipient over Smtp server`() {
        val emailDestination =
            EmailDestination("localhost", smtpPort, "none", "from@email.com", "test@localhost.com", "Smtp")
        val message = MessageContent(
            "Test Smtp email title",
            "Description for notification in text",
            "Description for notification in json encode html format",
            "opensearch.data",
            "base64",
            "VGVzdCBtZXNzYWdlCgo=",
            "application/octet-stream",
        )
        val response = Notification.sendMessage(emailDestination, message)
        assertEquals("Success", response.statusText)
        assertEquals(RestStatus.OK, response.statusCode)
    }

    fun `test send email with non-available host`() {
        val emailDestination =
            EmailDestination("invalidHost", smtpPort, "none", "from@email.com", "test@localhost.com", "Smtp")
        val message = MessageContent(
            "Test Smtp email title",
            "Description for notification in text",
            "Description for notification in json encode html format",
            "opensearch.data",
            "base64",
            "VGVzdCBtZXNzYWdlCgo=",
            "application/octet-stream",
        )
        val response = Notification.sendMessage(emailDestination, message)
        assertEquals(
            "sendEmail Error, status:Couldn't connect to host, port: invalidHost, $smtpPort; timeout -1",
            response.statusText
        )
        assertEquals(RestStatus.SERVICE_UNAVAILABLE, response.statusCode)
    }
}
