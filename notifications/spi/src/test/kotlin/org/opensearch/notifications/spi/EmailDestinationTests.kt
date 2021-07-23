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

package org.opensearch.notifications.spi

import io.mockk.every
import io.mockk.spyk
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.notifications.spi.client.DestinationEmailClient
import org.opensearch.notifications.spi.factory.DestinationFactoryProvider
import org.opensearch.notifications.spi.factory.SmtpEmailDestinationFactory
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.EmailDestination
import org.opensearch.rest.RestStatus
import javax.mail.MessagingException

@ExtendWith(MockitoExtension::class)
internal class EmailDestinationTests {

    @Test
    @Throws(Exception::class)
    fun testSmtpEmailMessage() {
        val expectedEmailResponse = DestinationMessageResponse(RestStatus.OK.status, "Success")
        val emailClient = spyk<DestinationEmailClient>()
        every { emailClient.sendMessage(any()) } returns Unit

        val smtpEmailDestinationFactory = SmtpEmailDestinationFactory(emailClient)
        DestinationFactoryProvider.destinationFactoryMap = mapOf(DestinationType.SMTP to smtpEmailDestinationFactory)

        val subject = "Test SMTP Email subject"
        val messageText = "{Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val destination = EmailDestination("abc", 465, "ssl", "test@abc.com", "to@abc.com", DestinationType.SMTP)

        val actualEmailResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message)
        assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        assertEquals(expectedEmailResponse.statusText, actualEmailResponse.statusText)
    }

    @Test
    @Throws(Exception::class)
    fun testSmtpFailingEmailMessage() {
        val expectedEmailResponse = DestinationMessageResponse(
            RestStatus.FAILED_DEPENDENCY.status,
            "Couldn't connect to host, port: localhost, 55555; timeout -1"
        )
        val emailClient = spyk<DestinationEmailClient>()
        every { emailClient.sendMessage(any()) } throws MessagingException(
            "Couldn't connect to host, port: localhost, 55555; timeout -1"
        )

        val smtpEmailDestinationFactory = SmtpEmailDestinationFactory(emailClient)
        DestinationFactoryProvider.destinationFactoryMap = mapOf(DestinationType.SMTP to smtpEmailDestinationFactory)

        val subject = "Test SMTP Email subject"
        val messageText = "{Vamshi Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val destination = EmailDestination(
            "localhost",
            55555,
            "none",
            "test@abc.com",
            "to@abc.com",
            DestinationType.SMTP
        )

        val actualEmailResponse: DestinationMessageResponse = NotificationSpi.sendMessage(destination, message)

        assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        assertEquals("sendEmail Error, status:${expectedEmailResponse.statusText}", actualEmailResponse.statusText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHostMissingEmailDestination() {
        try {
            EmailDestination("", 465, "ssl", "from@test.com", "to@test.com", DestinationType.SMTP)
        } catch (exception: Exception) {
            Assert.assertEquals("Host name should be provided", exception.message)
            throw exception
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidPortEmailDestination() {
        try {
            EmailDestination("localhost", -1, "ssl", "from@test.com", "to@test.com", DestinationType.SMTP)
        } catch (exception: Exception) {
            Assert.assertEquals("Port should be positive value", exception.message)
            throw exception
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMissingFromOrRecipientEmailDestination() {
        try {
            EmailDestination("localhost", 465, "ssl", "", "to@test.com", DestinationType.SMTP)
        } catch (exception: Exception) {
            Assert.assertEquals("FromAddress and recipient should be provided", exception.message)
            throw exception
        }
    }
}
