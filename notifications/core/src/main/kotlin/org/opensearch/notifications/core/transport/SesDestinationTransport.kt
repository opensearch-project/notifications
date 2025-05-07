/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.transport

import jakarta.mail.MessagingException
import jakarta.mail.internet.AddressException
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.client.DestinationClientPool
import org.opensearch.notifications.core.client.DestinationSesClient
import org.opensearch.notifications.core.utils.OpenForTesting
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SesDestination
import java.io.IOException

/**
 * This class handles the client responsible for submitting the messages to all types of email destinations.
 */
internal class SesDestinationTransport : DestinationTransport<SesDestination> {

    private val log by logger(SesDestinationTransport::class.java)
    private val destinationEmailClient: DestinationSesClient

    constructor() {
        this.destinationEmailClient = DestinationClientPool.sesClient
    }

    @OpenForTesting
    constructor(destinationSesClient: DestinationSesClient) {
        this.destinationEmailClient = destinationSesClient
    }

    override fun sendMessage(
        destination: SesDestination,
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
