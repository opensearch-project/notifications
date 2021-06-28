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

package org.opensearch.notifications.spi.client

import com.sun.mail.util.MailConnectException
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.EmailDestination
import org.opensearch.notifications.spi.utils.SecurityAccess
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.util.Properties
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.SendFailedException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

/**
 * This class handles the connections to the given Destination.
 */
class DestinationEmailClient {

    companion object {
        private val log by logger(DestinationEmailClient::class.java)
        // TODO get constants from config
        private const val SMTP_EMAIL_SIZE_LIMIT = 10000000
        private const val MINIMUM_EMAIL_HEADER_LENGTH = 160 // minimum value from 100 reference emails
    }

    @Throws(Exception::class)
    fun execute(emailDestination: EmailDestination, message: MessageContent): DestinationMessageResponse {

        if (isMessageSizeOverLimit(message)) {
            return DestinationMessageResponse(
                RestStatus.REQUEST_ENTITY_TOO_LARGE,
                "Email size larger than $SMTP_EMAIL_SIZE_LIMIT"
            )
        }

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = emailDestination.host
        prop["mail.smtp.port"] = emailDestination.port
        val session = Session.getInstance(prop)

        when (emailDestination.method) {
            "ssl" -> prop["mail.smtp.ssl.enable"] = true
            "start_tls" -> prop["mail.smtp.starttls.enable"] = true
            "none" -> {}
            else -> throw IllegalArgumentException("Invalid method supplied")
        }

        // prepare mimeMessage
        val mimeMessage = EmailMimeProvider.prepareMimeMessage(session, emailDestination, message)

        // send Mime Message
        return sendMimeMessage(mimeMessage)
    }

    /**
     * {@inheritDoc}
     */
    private fun sendMimeMessage(mimeMessage: MimeMessage): DestinationMessageResponse {
        return try {
            log.debug("Sending Email-SMTP")
            SecurityAccess.doPrivileged { sendMessage(mimeMessage) }
            log.info("Email-SMTP sent")
            DestinationMessageResponse(RestStatus.OK, "Success")
        } catch (exception: SendFailedException) {
            DestinationMessageResponse(RestStatus.BAD_GATEWAY, getMessagingExceptionText(exception))
        } catch (exception: MailConnectException) {
            DestinationMessageResponse(RestStatus.SERVICE_UNAVAILABLE, getMessagingExceptionText(exception))
        } catch (exception: MessagingException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY, getMessagingExceptionText(exception))
        }
    }

    /*
     * This method is useful for mocking the client
     */
    @Throws(Exception::class)
    fun sendMessage(msg: Message?) {
        Transport.send(msg)
    }

    /**
     * Create error string from MessagingException
     * @param exception Messaging Exception
     * @return generated error string
     */
    private fun getMessagingExceptionText(exception: MessagingException): String {
        log.info("EmailException $exception")
        return "sendEmail Error, status:${exception.message}"
    }

    private fun isMessageSizeOverLimit(message: MessageContent): Boolean {

        val approxAttachmentLength = if (message.fileData != null && message.fileName != null) {
            MINIMUM_EMAIL_HEADER_LENGTH + message.fileData.length + message.fileName.length
        } else {
            0
        }

        val approxEmailLength = MINIMUM_EMAIL_HEADER_LENGTH +
            message.title.length +
            message.textDescription.length +
            (message.htmlDescription?.length ?: 0) +
            approxAttachmentLength

        return approxEmailLength > SMTP_EMAIL_SIZE_LIMIT
    }
}
