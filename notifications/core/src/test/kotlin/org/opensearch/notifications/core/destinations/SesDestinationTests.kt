package org.opensearch.notifications.core.destinations

import com.amazonaws.services.simpleemail.model.SendRawEmailResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.easymock.EasyMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.NotificationCoreImpl
import org.opensearch.notifications.core.client.DestinationSesClient
import org.opensearch.notifications.core.credentials.SesClientFactory
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.SesDestinationTransport
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.SesDestination

@ExtendWith(MockitoExtension::class)
internal class SesDestinationTests {
    @Test
    fun testSesEmailMessage() {
        val expectedEmailResponse = DestinationMessageResponse(RestStatus.OK.status, "Success:test-message-id")
        val mockSesClientFactory: SesClientFactory = EasyMock.createMock(SesClientFactory::class.java)
        val sesClient = spyk(DestinationSesClient(mockSesClientFactory))

        val result: SendRawEmailResult = mockk()
        every { result.messageId } returns "test-message-id"
        val response: DestinationMessageResponse = mockk()
        every { sesClient.execute(any(), any(), any()) } returns response
        every { response.statusCode } returns RestStatus.OK.status
        every { response.statusText } returns "Success:${result.messageId}"

        val sesEmailDestinationTransport = SesDestinationTransport(sesClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SES to sesEmailDestinationTransport)

        val subject = "Test SES Email subject"
        val messageText = "{Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present}"
        val message = MessageContent(subject, messageText)
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"
        val destination = SesDestination("testAccountName", "us-east-1", roleArn, "test@from.com", "to@abc.com")

        val actualEmailResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")
        Assertions.assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        Assertions.assertEquals(expectedEmailResponse.statusText, actualEmailResponse.statusText)
    }

    @Test
    fun testSesEmailHtmlMessage() {
        val expectedEmailResponse = DestinationMessageResponse(RestStatus.OK.status, "Success:test-message-id")
        val mockSesClientFactory: SesClientFactory = EasyMock.createMock(SesClientFactory::class.java)
        val sesClient = spyk(DestinationSesClient(mockSesClientFactory))

        val result: SendRawEmailResult = mockk()
        every { result.messageId } returns "test-message-id"
        val response: DestinationMessageResponse = mockk()
        every { sesClient.execute(any(), any(), any()) } returns response
        every { response.statusCode } returns RestStatus.OK.status
        every { response.statusText } returns "Success:${result.messageId}"

        val sesEmailDestinationTransport = SesDestinationTransport(sesClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.SES to sesEmailDestinationTransport)

        val subject = "Test SMTP Email with html body subject"
        val htmlBody = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body>\n" +
            "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>"
        val message = MessageContent(subject, "", htmlBody)
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"
        val destination = SesDestination("testAccountName", "us-east-1", roleArn, "test@from.com", "to@abc.com")

        val actualEmailResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")
        Assertions.assertEquals(expectedEmailResponse.statusCode, actualEmailResponse.statusCode)
        Assertions.assertEquals(expectedEmailResponse.statusText, actualEmailResponse.statusText)
    }
}
