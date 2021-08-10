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

import org.opensearch.notifications.spi.NotificationSpiPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.spi.credentials.SesClientFactory
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SesDestination
import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.SecurityAccess
import org.opensearch.notifications.spi.utils.logger
import org.opensearch.rest.RestStatus
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.model.AccountSendingPausedException
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException
import software.amazon.awssdk.services.ses.model.ConfigurationSetSendingPausedException
import software.amazon.awssdk.services.ses.model.MailFromDomainNotVerifiedException
import software.amazon.awssdk.services.ses.model.MessageRejectedException
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest
import software.amazon.awssdk.services.ses.model.SesException
import java.io.ByteArrayOutputStream
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
            val region = Region.of(sesAwsRegion)
            val client = sesClientFactory.createSesClient(region, roleArn)
            val outputStream = ByteArrayOutputStream()
            SecurityAccess.doPrivileged { mimeMessage.writeTo(outputStream) }
            val emailSize = outputStream.size()
            if (emailSize <= PluginSettings.emailSizeLimit) {
                val data = SdkBytes.fromByteArray(outputStream.toByteArray())
                val rawMessage = RawMessage.builder()
                    .data(data)
                    .build()
                val rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build()
                val response = SecurityAccess.doPrivileged { client.sendRawEmail(rawEmailRequest) }
                log.info("$LOG_PREFIX:Email-SES:$referenceId status:$response")
                DestinationMessageResponse(RestStatus.OK.status, "Success")
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
        } catch (exception: SesException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSesExceptionText(exception))
        } catch (exception: SdkException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSdkExceptionText(exception))
        }
    }

    /**
     * Create error string from Amazon SES Exceptions
     * @param exception SES Exception
     * @return generated error string
     */
    private fun getSesExceptionText(exception: SesException): String {
        val httpResponse = exception.awsErrorDetails().sdkHttpResponse()
        log.info("$LOG_PREFIX:SesException $exception")
        return "sendEmail Error, SES status:${httpResponse.statusCode()}:${httpResponse.statusText()}"
    }

    /**
     * Create error string from Amazon SDK Exceptions
     * @param exception SDK Exception
     * @return generated error string
     */
    private fun getSdkExceptionText(exception: SdkException): String {
        log.info("$LOG_PREFIX:SdkException $exception")
        return "sendEmail Error, SDK status:${exception.message}"
    }
}
