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

package org.opensearch.notifications.core

import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.common.settings.SecureString
import org.opensearch.notifications.core.client.DestinationSmtpClient
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.SmtpDestinationTransport
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.SecureDestinationSettings
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SmtpDestination
import org.opensearch.rest.RestStatus
import javax.mail.MessagingException

@ExtendWith(MockitoExtension::class)
internal class SmtpDestinationTests {

    @Test
    fun testSmtpEmailMessage() {
        val expectedEmailResponse = DestinationMessageResponse(RestStatus.OK.status, "Success")
        val emailClient = spyk<DestinationSmtpClient>()
        every { emailClient.sendMessage(any()) } returns Unit

        val smtpEmailDestinationTransport = SmtpDestinationTransport(emailClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to smtpEmailDestinationTransport)

        val subject = "Test SMTP Email subject"
        val messageText = "{Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val destination = SmtpDestination("testAccountName", "abc", 465, "ssl", "test@abc.com", "to@abc.com")

        val actualEmailResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")
        assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        assertEquals(expectedEmailResponse.statusText, actualEmailResponse.statusText)
    }

    @Test
    fun `test auth email`() {
        val expectedEmailResponse = DestinationMessageResponse(RestStatus.OK.status, "Success")
        val emailClient = spyk<DestinationSmtpClient>()
        every { emailClient.sendMessage(any()) } returns Unit

        val username = SecureString("user1".toCharArray())
        val password = SecureString("password".toCharArray())
        every { emailClient.getSecureDestinationSetting(any()) } returns SecureDestinationSettings(username, password)

        val smtpDestinationTransport = SmtpDestinationTransport(emailClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to smtpDestinationTransport)

        val subject = "Test SMTP Email subject"
        val messageText = "{Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val destination = SmtpDestination("testAccountName", "abc", 465, "ssl", "test@abc.com", "to@abc.com")

        val actualEmailResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")
        assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        assertEquals(expectedEmailResponse.statusText, actualEmailResponse.statusText)
    }

    @Test
    fun testSmtpFailingEmailMessage() {
        val expectedEmailResponse = DestinationMessageResponse(
            RestStatus.FAILED_DEPENDENCY.status,
            "Couldn't connect to host, port: localhost, 55555; timeout -1"
        )
        val emailClient = spyk<DestinationSmtpClient>()
        every { emailClient.sendMessage(any()) } throws MessagingException(
            "Couldn't connect to host, port: localhost, 55555; timeout -1"
        )

        val smtpEmailDestinationTransport = SmtpDestinationTransport(emailClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SMTP to smtpEmailDestinationTransport)

        val subject = "Test SMTP Email subject"
        val messageText = "{Vamshi Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val destination = SmtpDestination(
            "testAccountName",
            "localhost",
            55555,
            "none",
            "test@abc.com",
            "to@abc.com"
        )

        val actualEmailResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        assertEquals("sendEmail Error, status:${expectedEmailResponse.statusText}", actualEmailResponse.statusText)
    }

    @Test
    fun testHostMissingEmailDestination() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            SmtpDestination("testAccountName", "", 465, "ssl", "from@test.com", "to@test.com")
        }
        assertEquals("Host name should be provided", exception.message)
    }

    @Test
    fun testInvalidPortEmailDestination() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            SmtpDestination("testAccountName", "localhost", -1, "ssl", "from@test.com", "to@test.com")
        }
        assertEquals("Port should be positive value", exception.message)
    }

    @Test
    fun testMissingFromOrRecipientEmailDestination() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            SmtpDestination("testAccountName", "localhost", 465, "ssl", "", "to@test.com")
        }
        assertEquals("FromAddress and recipient should be provided", exception.message)
    }
}
