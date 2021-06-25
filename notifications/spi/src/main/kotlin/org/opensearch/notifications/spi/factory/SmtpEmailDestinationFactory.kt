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
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.EmailDestination
import org.opensearch.notifications.spi.utils.OpenForTesting
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.internet.AddressException

/**
 * This class handles the client responsible for submitting the messages to all types of email destinations.
 */
internal class SmtpEmailDestinationFactory : DestinationFactory<EmailDestination> {

    private val log by logger(SmtpEmailDestinationFactory::class.java)
    private val destinationEmailClient: DestinationEmailClient

    constructor() {
        this.destinationEmailClient = DestinationClientPool.emailClient
    }

    @OpenForTesting
    constructor(destinationEmailClient: DestinationEmailClient) {
        this.destinationEmailClient = destinationEmailClient
    }

    override fun sendMessage(destination: EmailDestination, message: MessageContent): DestinationMessageResponse {
        return try {
            destinationEmailClient.execute(destination, message)
        } catch (addressException: AddressException) {
            log.error("Error sending Email: recipient parsing failed with status:${addressException.message}")
            DestinationMessageResponse(
                RestStatus.BAD_REQUEST,
                "recipient parsing failed with status:${addressException.message}"
            )
        } catch (messagingException: MessagingException) {
            log.error("Error sending Email: Email message creation failed with status:${messagingException.message}")
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${messagingException.message}"
            )
        } catch (ioException: IOException) {
            log.error("Error sending Email: Email message creation failed with status:${ioException.message}")
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${ioException.message}"
            )
        } catch (illegalArgumentException: IllegalArgumentException) {
            log.error(
                "Error sending Email: Email message creation failed with status:${illegalArgumentException.message}"
            )
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${illegalArgumentException.message}"
            )
        }
    }
}
