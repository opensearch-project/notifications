/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.destinations

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest
import com.amazonaws.services.simpleemail.model.SendRawEmailResult
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.client.DestinationSesClient
import org.opensearch.notifications.core.client.DestinationSnsClient
import org.opensearch.notifications.core.credentials.SesClientFactory
import org.opensearch.notifications.core.credentials.SnsClientFactory
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SesDestination
import org.opensearch.notifications.spi.model.destination.SnsDestination

/**
 * End-to-end propagation tests: verify the tenant-scoped [SnsDestination.roleSessionName] /
 * [SesDestination.roleSessionName] set on the send path actually reaches the STS-assuming client
 * factory (the argument that ultimately becomes the STS role session name). This guards the full
 * thread through [DestinationSnsClient] / [DestinationSesClient], which is the point of the change
 * (CloudTrail attribution) and the part most likely to silently regress.
 */
internal class RoleSessionNamePropagationTests {

    private val sessionName = "alerting-notification-3fa85f64-5717-4562-b3fc-2c963f66afa6"

    @Test
    fun `sns destination roleSessionName reaches the sns client factory`() {
        val snsClientFactory = mockk<SnsClientFactory>()
        val amazonSNS = mockk<AmazonSNS>()
        every {
            snsClientFactory.createSnsClient(any(), any(), any(), any(), any())
        } returns amazonSNS
        every { amazonSNS.publish(any<PublishRequest>()) } returns PublishResult().withMessageId("test-message-id")

        val destination = SnsDestination(
            topicArn = "arn:aws:sns:us-west-2:012345678912:test-notification",
            roleArn = "arn:aws:iam::012345678912:role/iam-test",
            roleSessionName = sessionName
        )

        DestinationSnsClient(snsClientFactory).execute(destination, MessageContent("title", "body"), "ref")

        verify(exactly = 1) { snsClientFactory.createSnsClient(any(), any(), any(), any(), sessionName) }
    }

    @Test
    fun `ses destination roleSessionName reaches the ses client factory`() {
        val sesClientFactory = mockk<SesClientFactory>()
        val amazonSES = mockk<AmazonSimpleEmailService>()
        every {
            sesClientFactory.createSesClient(any(), any(), any(), any(), any())
        } returns amazonSES
        every { amazonSES.sendRawEmail(any<SendRawEmailRequest>()) } returns SendRawEmailResult().withMessageId("test-message-id")

        val destination = SesDestination(
            accountName = "test-account",
            awsRegion = "us-west-2",
            roleArn = "arn:aws:iam::012345678912:role/iam-test",
            fromAddress = "from@example.com",
            recipient = "to@example.com",
            roleSessionName = sessionName
        )

        DestinationSesClient(sesClientFactory).execute(destination, MessageContent("title", "body"), "ref")

        verify(exactly = 1) { sesClientFactory.createSesClient(any(), any(), any(), any(), sessionName) }
    }
}
