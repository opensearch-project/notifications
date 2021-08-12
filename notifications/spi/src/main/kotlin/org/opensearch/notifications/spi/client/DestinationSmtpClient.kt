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
import org.opensearch.common.settings.SecureString
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.SecureDestinationSettings
import org.opensearch.notifications.spi.model.destination.SmtpDestination
import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.SecurityAccess
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.SendFailedException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

/**
 * This class handles the connections to the given Destination.
 */
class DestinationSmtpClient {

    companion object {
        private val log by logger(DestinationSmtpClient::class.java)
    }

    @Throws(Exception::class)
    fun execute(
        smtpDestination: SmtpDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        if (EmailMessageValidator.isMessageSizeOverLimit(message)) {
            return DestinationMessageResponse(
                RestStatus.REQUEST_ENTITY_TOO_LARGE.status,
                "Email size larger than ${PluginSettings.emailSizeLimit}"
            )
        }

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = smtpDestination.host
        prop["mail.smtp.port"] = smtpDestination.port
        var session = Session.getInstance(prop)

        when (smtpDestination.method) {
            "ssl" -> prop["mail.smtp.ssl.enable"] = true
            "start_tls" -> prop["mail.smtp.starttls.enable"] = true
            "none" -> {
            }
            else -> throw IllegalArgumentException("Invalid method supplied")
        }

        if (smtpDestination.method != "none") {
            val secureDestinationSetting = getSecureDestinationSetting(smtpDestination)
            if (secureDestinationSetting != null) {
                prop["mail.smtp.auth"] = true
                session = Session.getInstance(
                    prop,
                    object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(
                                secureDestinationSetting.emailUsername.toString(),
                                secureDestinationSetting.emailPassword.toString()
                            )
                        }
                    }
                )
            }
        }

        // prepare mimeMessage
        val mimeMessage = EmailMimeProvider.prepareMimeMessage(
            session,
            smtpDestination.fromAddress,
            smtpDestination.recipient,
            message
        )

        // send Mime Message
        return sendMimeMessage(mimeMessage, referenceId)
    }

    fun getSecureDestinationSetting(SmtpDestination: SmtpDestination): SecureDestinationSettings? {
        val emailUsername: SecureString? =
            PluginSettings.destinationSettings[SmtpDestination.accountName]?.emailUsername
        val emailPassword: SecureString? =
            PluginSettings.destinationSettings[SmtpDestination.accountName]?.emailPassword
        return if (emailUsername == null || emailPassword == null) {
            null
        } else {
            SecureDestinationSettings(emailUsername, emailPassword)
        }
    }

    /**
     * {@inheritDoc}
     */
    private fun sendMimeMessage(mimeMessage: MimeMessage, referenceId: String): DestinationMessageResponse {
        return try {
            log.debug("Sending Email-SMTP for $referenceId")
            SecurityAccess.doPrivileged { sendMessage(mimeMessage) }
            log.info("Email-SMTP sent for $referenceId")
            DestinationMessageResponse(RestStatus.OK.status, "Success")
        } catch (exception: SendFailedException) {
            DestinationMessageResponse(RestStatus.BAD_GATEWAY.status, getMessagingExceptionText(exception))
        } catch (exception: MailConnectException) {
            DestinationMessageResponse(RestStatus.SERVICE_UNAVAILABLE.status, getMessagingExceptionText(exception))
        } catch (exception: MessagingException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getMessagingExceptionText(exception))
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
}
