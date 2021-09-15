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

package org.opensearch.notifications.core.transport

import org.opensearch.notifications.core.client.DestinationClientPool
import org.opensearch.notifications.core.client.DestinationSmtpClient
import org.opensearch.notifications.core.spi.model.DestinationMessageResponse
import org.opensearch.notifications.core.spi.model.MessageContent
import org.opensearch.notifications.core.spi.model.destination.SmtpDestination
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.rest.RestStatus
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.internet.AddressException

/**
 * This class handles the client responsible for submitting the messages to all types of email destinations.
 */
internal class SmtpDestinationTransport : DestinationTransport<SmtpDestination> {

    private val log by logger(SmtpDestinationTransport::class.java)
    private val destinationEmailClient: DestinationSmtpClient

    constructor() {
        this.destinationEmailClient = DestinationClientPool.smtpClient
    }

    @OpenForTesting
    constructor(destinationSmtpClient: DestinationSmtpClient) {
        this.destinationEmailClient = destinationSmtpClient
    }

    override fun sendMessage(
        destination: SmtpDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        return try {
            destinationEmailClient.execute(destination, message, referenceId)
        } catch (addressException: AddressException) {
            log.error("Error sending Email: recipient parsing failed with status:${addressException.message}")
            DestinationMessageResponse(
                RestStatus.BAD_REQUEST.status,
                "recipient parsing failed with status:${addressException.message}"
            )
        } catch (messagingException: MessagingException) {
            log.error("Error sending Email: Email message creation failed with status:${messagingException.message}")
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY.status,
                "Email message creation failed with status:${messagingException.message}"
            )
        } catch (ioException: IOException) {
            log.error("Error sending Email: Email message creation failed with status:${ioException.message}")
            DestinationMessageResponse(
                RestStatus.FAILED_DEPENDENCY.status,
                "Email message creation failed with status:${ioException.message}"
            )
        } catch (illegalArgumentException: IllegalArgumentException) {
            log.error(
                "Error sending Email: Email message creation failed with status:${illegalArgumentException.message}"
            )
            DestinationMessageResponse(
                RestStatus.BAD_REQUEST.status,
                "Email message creation failed with status:${illegalArgumentException.message}"
            )
        }
    }
}
