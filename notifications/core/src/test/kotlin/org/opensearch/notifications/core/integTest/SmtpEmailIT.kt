/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.integTest

import org.junit.After
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SmtpDestination
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

    fun `test send email to one recipient over smtp server`() {
        val smtpDestination = SmtpDestination(
            "testAccountName",
            "localhost",
            smtpPort,
            "none",
            "from@email.com",
            "test@localhost.com"
        )
        val message = MessageContent(
            "Test smtp email title",
            "Description for notification in text",
            "Description for notification in json encode html format",
            "opensearch.data",
            "base64",
            "VGVzdCBtZXNzYWdlCgo=",
            "application/octet-stream",
        )
        val response = NotificationCoreImpl.sendMessage(smtpDestination, message, "ref")
        assertEquals("Success", response.statusText)
        assertEquals(RestStatus.OK.status, response.statusCode)
    }

    fun `test send email with non-available host`() {
        val smtpDestination = SmtpDestination(
            "testAccountName",
            "invalidHost",
            smtpPort,
            "none",
            "from@email.com",
            "test@localhost.com"
        )
        val message = MessageContent(
            "Test smtp email title",
            "Description for notification in text",
            "Description for notification in json encode html format",
            "opensearch.data",
            "base64",
            "VGVzdCBtZXNzYWdlCgo=",
            "application/octet-stream",
        )
        val response = NotificationCoreImpl.sendMessage(smtpDestination, message, "ref")
        assertEquals(
            "sendEmail Error, status:Couldn't connect to host, port: invalidHost, $smtpPort; timeout -1",
            response.statusText
        )
        assertEquals(RestStatus.SERVICE_UNAVAILABLE.status, response.statusCode)
    }
}
