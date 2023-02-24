/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.smtp

import org.junit.After
import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.SmtpDestinationTransport
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SmtpDestination
import org.opensearch.rest.RestStatus
import org.springframework.integration.test.mail.TestMailServer
import kotlin.test.assertEquals

class SmtpEmailTests {

    internal companion object {
        private const val smtpPort = 10255 // use non-standard port > 1024 to avoid permission issue
        private val smtpServer = TestMailServer.smtp(smtpPort)
    }

    @After
    fun tearDownServer() {
        smtpServer.stop()
        smtpServer.resetServer()
    }

    @Test
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
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to SmtpDestinationTransport())
        val response = NotificationCoreImpl.sendMessage(smtpDestination, message, "ref")
        assertEquals("Success", response.statusText)
        assertEquals(RestStatus.OK.status, response.statusCode)
    }

    @Test
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
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to SmtpDestinationTransport())
        val response = NotificationCoreImpl.sendMessage(smtpDestination, message, "ref")
        assertEquals(
            "sendEmail Error, status:Couldn't connect to host, port: invalidHost, $smtpPort; timeout -1",
            response.statusText
        )
        assertEquals(RestStatus.SERVICE_UNAVAILABLE.status, response.statusCode)
    }
    @Test
    fun `test send HTML email over smtp server`() {
        val smtpDestination = SmtpDestination(
            "testAccountName",
            "localhost",
            smtpPort,
            "none",
            "from@email.com",
            "test@localhost.com"
        )
        val htmlContent = "<html><body><h1>This  is an HTML email test</h1><p>It should display this text in a web browser</p></body></html>"
        val message = MessageContent(
            "Test HTML email title",
            "Description for notification in text",
            htmlContent,
            "opensearch.data",
            "base64",
            "VGVzdCBtZXNzYWdlCgo=",
            "application/octet-stream",
        )
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to SmtpDestinationTransport())
        val response = NotificationCoreImpl.sendMessage(smtpDestination, message, "ref")
        assertEquals("Success", response.statusText)
        assertEquals(RestStatus.OK.status, response.statusCode)
    }
}
