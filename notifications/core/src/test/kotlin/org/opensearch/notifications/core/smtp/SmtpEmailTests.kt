/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.smtp

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.SmtpDestinationTransport
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SmtpDestination
import kotlin.test.assertEquals

class SmtpEmailTests {
    private lateinit var greenMail: GreenMail

    @BeforeEach
    fun setUpServer() {
        greenMail = GreenMail(ServerSetupTest.SMTP)
        greenMail.start()
    }

    @AfterEach
    fun tearDownServer() {
        greenMail.stop()
    }

    @Test
    fun `test send email to one recipient over smtp server`() {
        val smtpDestination =
            SmtpDestination(
                "testAccountName",
                "localhost",
                ServerSetupTest.SMTP.port,
                "none",
                "from@email.com",
                "test@localhost.com",
            )
        val message =
            MessageContent(
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
        assertEquals(1, greenMail.receivedMessages.size) // Indicates retrieval of notification.
    }

    @Test
    fun `test send email with non-available host`() {
        val smtpDestination =
            SmtpDestination(
                "testAccountName",
                "invalidHost",
                ServerSetupTest.SMTP.port,
                "none",
                "from@email.com",
                "test@localhost.com",
            )
        val message =
            MessageContent(
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
            "sendEmail Error, status:Couldn't connect to host, port: invalidHost, ${ServerSetupTest.SMTP.port}; timeout -1",
            response.statusText,
        )
        assertEquals(RestStatus.SERVICE_UNAVAILABLE.status, response.statusCode)
    }
}
