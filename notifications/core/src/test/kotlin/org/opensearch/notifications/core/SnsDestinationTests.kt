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

package org.opensearch.notifications.core

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkBaseException
import com.amazonaws.services.sns.model.AmazonSNSException
import com.amazonaws.services.sns.model.PublishResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.easymock.EasyMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.client.DestinationSnsClient
import org.opensearch.notifications.core.credentials.SnsClientFactory
import org.opensearch.notifications.core.spi.model.DestinationMessageResponse
import org.opensearch.notifications.core.spi.model.MessageContent
import org.opensearch.notifications.core.spi.model.destination.DestinationType
import org.opensearch.notifications.core.spi.model.destination.SnsDestination
import org.opensearch.notifications.core.transport.DestinationTransportProvider
import org.opensearch.notifications.core.transport.SnsDestinationTransport
import org.opensearch.rest.RestStatus

internal class SnsDestinationTests {

    @Test
    fun `test sns message success response`() {
        val expectedSnsResponse = DestinationMessageResponse(RestStatus.OK.status, "Success, message id: test-message-id")
        val mockSnsClientFactory: SnsClientFactory = EasyMock.createMock(SnsClientFactory::class.java)
        val destinationSnsClient = spyk(DestinationSnsClient(mockSnsClientFactory))
        val publishResult: PublishResult = mockk()
        every { publishResult.messageId } returns "test-message-id"
        every { destinationSnsClient.sendMessage(any(), any(), any()) } returns publishResult

        val snsDestinationTransport = SnsDestinationTransport(destinationSnsClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.SNS to snsDestinationTransport
        )

        val title = "test sns"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val topicArn = "arn:aws:sns:us-west-2:012345678912:test-notification"
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"

        val destination = SnsDestination(topicArn, roleArn)
        val message = MessageContent(title, messageText)

        val actualSnsResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedSnsResponse.statusText, actualSnsResponse.statusText)
        assertEquals(expectedSnsResponse.statusCode, actualSnsResponse.statusCode)
    }

    @Test
    fun `test sns send exception response`() {
        val expectedSnsResponse = DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, "SNS Send Error(424), SNS status:(test-exception-name)123:test-exception-message")
        val mockSnsClientFactory: SnsClientFactory = EasyMock.createMock(SnsClientFactory::class.java)
        val destinationSnsClient = spyk(DestinationSnsClient(mockSnsClientFactory))
        val publishResult: PublishResult = mockk()
        val amazonSNSException: AmazonSNSException = mockk()
        every { amazonSNSException.statusCode } returns 424
        every { amazonSNSException.errorType.name } returns "test-exception-name"
        every { amazonSNSException.errorCode } returns "123"
        every { amazonSNSException.errorMessage } returns "test-exception-message"
        every { publishResult.messageId } throws amazonSNSException
        every { destinationSnsClient.sendMessage(any(), any(), any()) } returns publishResult

        val snsDestinationTransport = SnsDestinationTransport(destinationSnsClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.SNS to snsDestinationTransport
        )

        val title = "test sns"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val topicArn = "arn:aws:sns:us-west-2:012345678912:test-notification"
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"

        val destination = SnsDestination(topicArn, roleArn)
        val message = MessageContent(title, messageText)

        val actualSnsResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedSnsResponse.statusText, actualSnsResponse.statusText)
        assertEquals(expectedSnsResponse.statusCode, actualSnsResponse.statusCode)
    }

    @Test
    fun `test sns service exception response`() {
        val expectedSnsResponse = DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, "SNS service Error(424), Service status:(test-exception-name)123:test-exception-message")
        val mockSnsClientFactory: SnsClientFactory = EasyMock.createMock(SnsClientFactory::class.java)
        val destinationSnsClient = spyk(DestinationSnsClient(mockSnsClientFactory))
        val publishResult: PublishResult = mockk()
        val amazonServiceException: AmazonServiceException = mockk()
        every { amazonServiceException.statusCode } returns 424
        every { amazonServiceException.errorType.name } returns "test-exception-name"
        every { amazonServiceException.errorCode } returns "123"
        every { amazonServiceException.errorMessage } returns "test-exception-message"
        every { publishResult.messageId } throws amazonServiceException
        every { destinationSnsClient.sendMessage(any(), any(), any()) } returns publishResult

        val snsDestinationTransport = SnsDestinationTransport(destinationSnsClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.SNS to snsDestinationTransport
        )

        val title = "test sns"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val topicArn = "arn:aws:sns:us-west-2:012345678912:test-notification"
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"

        val destination = SnsDestination(topicArn, roleArn)
        val message = MessageContent(title, messageText)

        val actualSnsResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedSnsResponse.statusText, actualSnsResponse.statusText)
        assertEquals(expectedSnsResponse.statusCode, actualSnsResponse.statusCode)
    }

    @Test
    fun `test sns sdk exception response`() {
        val expectedSnsResponse = DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, "SNS sdk Error, SDK status:test-exception-message")
        val mockSnsClientFactory: SnsClientFactory = EasyMock.createMock(SnsClientFactory::class.java)
        val destinationSnsClient = spyk(DestinationSnsClient(mockSnsClientFactory))
        val publishResult: PublishResult = mockk()
        val sdkBaseException: SdkBaseException = mockk()
        every { sdkBaseException.message } returns "test-exception-message"
        every { publishResult.messageId } throws sdkBaseException
        every { destinationSnsClient.sendMessage(any(), any(), any()) } returns publishResult

        val snsDestinationTransport = SnsDestinationTransport(destinationSnsClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(
            DestinationType.SNS to snsDestinationTransport
        )

        val title = "test sns"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member callout: " +
            "@All All Present member callout: @Present"
        val topicArn = "arn:aws:sns:us-west-2:012345678912:test-notification"
        val roleArn = "arn:aws:iam::012345678912:role/iam-test"

        val destination = SnsDestination(topicArn, roleArn)
        val message = MessageContent(title, messageText)

        val actualSnsResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "ref")

        assertEquals(expectedSnsResponse.statusText, actualSnsResponse.statusText)
        assertEquals(expectedSnsResponse.statusCode, actualSnsResponse.statusCode)
    }
}
