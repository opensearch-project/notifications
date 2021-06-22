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

package org.opensearch.notifications.spi.factory

import org.opensearch.notifications.spi.client.DestinationClientPool
import org.opensearch.notifications.spi.client.DestinationEmailClient
import org.opensearch.notifications.spi.client.DestinationEmailClientPool
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.EmailDestination
import org.opensearch.notifications.spi.model.destination.WebhookDestination
import org.opensearch.notifications.spi.utils.OpenForTesting
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.internet.AddressException

/**
 * This class handles the client responsible for submitting the messages to all types of webhook destinations.
 */
internal class SmtpEmailDestinationFactory : DestinationFactory<EmailDestination> {

    private val log by logger(SmtpEmailDestinationFactory::class.java)
    private val destinationEmailClient: DestinationEmailClient

    constructor() {
        this.destinationEmailClient = DestinationEmailClientPool.emailClient
    }

    @OpenForTesting
    constructor(destinationEmailClient: DestinationEmailClient) {
        this.destinationEmailClient = destinationEmailClient
    }

    override fun sendMessage(destination: EmailDestination, message: MessageContent): DestinationMessageResponse {
        return try {
            destinationEmailClient.execute(destination, message)

        } catch (addressException: AddressException) {
            DestinationMessageResponse(
                RestStatus.BAD_REQUEST,
                "recipient parsing failed with status:${addressException.message}"
            )
        } catch (messagingException: MessagingException) {
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${messagingException.message}"
            )
        } catch (ioException: IOException) {
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${ioException.message}"
            )
        }
    }
}