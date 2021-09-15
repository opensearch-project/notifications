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

package org.opensearch.notifications.core.client

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkBaseException
import com.amazonaws.services.simpleemail.model.AccountSendingPausedException
import com.amazonaws.services.simpleemail.model.AmazonSimpleEmailServiceException
import com.amazonaws.services.simpleemail.model.ConfigurationSetDoesNotExistException
import com.amazonaws.services.simpleemail.model.ConfigurationSetSendingPausedException
import com.amazonaws.services.simpleemail.model.MailFromDomainNotVerifiedException
import com.amazonaws.services.simpleemail.model.MessageRejectedException
import com.amazonaws.services.simpleemail.model.RawMessage
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest
import org.opensearch.notifications.core.NotificationCorePlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.core.credentials.SesClientFactory
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.spi.model.DestinationMessageResponse
import org.opensearch.notifications.core.spi.model.MessageContent
import org.opensearch.notifications.core.spi.model.destination.SesDestination
import org.opensearch.notifications.core.utils.SecurityAccess
import org.opensearch.notifications.core.utils.logger
import org.opensearch.rest.RestStatus
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.MimeMessage

/**
 * This class handles the connections to the given Destination.
 */
class DestinationSesClient(private val sesClientFactory: SesClientFactory) {

    companion object {
        private val log by logger(DestinationSesClient::class.java)
    }

    /**
     * {@inheritDoc}
     */
    private fun prepareSession(): Session {
        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        return Session.getInstance(prop)
    }

    @Throws(Exception::class)
    fun execute(
        sesDestination: SesDestination,
        message: MessageContent,
        referenceId: String
    ): DestinationMessageResponse {
        if (EmailMessageValidator.isMessageSizeOverLimit(message)) {
            return DestinationMessageResponse(
                RestStatus.REQUEST_ENTITY_TOO_LARGE.status,
                "Email size larger than ${PluginSettings.emailSizeLimit}"
            )
        }

        // prepare session
        val session = prepareSession()
        // prepare mimeMessage
        val mimeMessage = EmailMimeProvider.prepareMimeMessage(
            session,
            sesDestination.fromAddress,
            sesDestination.recipient,
            message
        )
        // send Mime Message
        return sendMimeMessage(referenceId, sesDestination.awsRegion, sesDestination.roleArn, mimeMessage)
    }

    /**
     * {@inheritDoc}
     */
    private fun sendMimeMessage(
        referenceId: String,
        sesAwsRegion: String,
        roleArn: String?,
        mimeMessage: MimeMessage
    ): DestinationMessageResponse {
        return try {
            log.debug("$LOG_PREFIX:Sending Email-SES:$referenceId")
            val client = sesClientFactory.createSesClient(sesAwsRegion, roleArn)
            val outputStream = ByteArrayOutputStream()
            SecurityAccess.doPrivileged { mimeMessage.writeTo(outputStream) }
            val emailSize = outputStream.size()
            if (emailSize <= PluginSettings.emailSizeLimit) {
                val rawMessage = RawMessage(ByteBuffer.wrap(outputStream.toByteArray()))
                val rawEmailRequest = SendRawEmailRequest(rawMessage)
                val response = SecurityAccess.doPrivileged { client.sendRawEmail(rawEmailRequest) }
                log.info("$LOG_PREFIX:Email-SES:$referenceId status:$response")
                DestinationMessageResponse(RestStatus.OK.status, "Success:${response.messageId}")
            } else {
                DestinationMessageResponse(
                    RestStatus.REQUEST_ENTITY_TOO_LARGE.status,
                    "Email size($emailSize) larger than ${PluginSettings.emailSizeLimit}"
                )
            }
        } catch (exception: MessageRejectedException) {
            DestinationMessageResponse(RestStatus.SERVICE_UNAVAILABLE.status, getSesExceptionText(exception))
        } catch (exception: MailFromDomainNotVerifiedException) {
            DestinationMessageResponse(RestStatus.FORBIDDEN.status, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetDoesNotExistException) {
            DestinationMessageResponse(RestStatus.NOT_IMPLEMENTED.status, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetSendingPausedException) {
            DestinationMessageResponse(RestStatus.SERVICE_UNAVAILABLE.status, getSesExceptionText(exception))
        } catch (exception: AccountSendingPausedException) {
            DestinationMessageResponse(RestStatus.INSUFFICIENT_STORAGE.status, getSesExceptionText(exception))
        } catch (exception: AmazonSimpleEmailServiceException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSesExceptionText(exception))
        } catch (exception: AmazonServiceException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getServiceExceptionText(exception))
        } catch (exception: SdkBaseException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSdkExceptionText(exception))
        }
    }

    /**
     * Create error string from Amazon SES Exceptions
     * @param exception SES Exception
     * @return generated error string
     */
    private fun getSesExceptionText(exception: AmazonSimpleEmailServiceException): String {
        log.info("$LOG_PREFIX:SesException $exception")
        return "sendEmail Error, SES status:${exception.errorMessage}"
    }

    /**
     * Create error string from Amazon Service Exceptions
     * @param exception Amazon Service Exception
     * @return generated error string
     */
    private fun getServiceExceptionText(exception: AmazonServiceException): String {
        log.info("$LOG_PREFIX:SesException $exception")
        return "sendEmail Error(${exception.statusCode}), SES status:(${exception.errorType.name})${exception.errorCode}:${exception.errorMessage}"
    }

    /**
     * Create error string from Amazon SDK Exceptions
     * @param exception SDK Exception
     * @return generated error string
     */
    private fun getSdkExceptionText(exception: SdkBaseException): String {
        log.info("$LOG_PREFIX:SdkException $exception")
        return "sendEmail Error, SDK status:${exception.message}"
    }
}
