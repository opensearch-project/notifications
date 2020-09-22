/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.notifications.channel

import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessage
import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.security.SecurityAccess
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import org.apache.logging.log4j.LogManager
import org.elasticsearch.rest.RestStatus
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.AccountSendingPausedException
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException
import software.amazon.awssdk.services.ses.model.ConfigurationSetSendingPausedException
import software.amazon.awssdk.services.ses.model.MailFromDomainNotVerifiedException
import software.amazon.awssdk.services.ses.model.MessageRejectedException
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest
import software.amazon.awssdk.services.ses.model.SesException
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.internet.AddressException
import javax.mail.internet.MimeMessage

/**
 * Notification channel for sending mail over Amazon SES.
 */
object SesChannel : NotificationChannel {
    private val log = LogManager.getLogger(javaClass)

    /**
     * {@inheritDoc}
     */
    override fun sendMessage(refTag: String, recipient: String, channelMessage: ChannelMessage): ChannelMessageResponse {
        val fromAddress = PluginSettings.emailFromAddress
        if (PluginSettings.UNCONFIGURED_EMAIL_ADDRESS == fromAddress) {
            return ChannelMessageResponse(RestStatus.NOT_IMPLEMENTED, "Email from: address not configured")
        }
        val mimeMessage: MimeMessage
        return try {
            mimeMessage = EmailMimeProvider.prepareMimeMessage(fromAddress, recipient, channelMessage)
            sendMimeMessage(refTag, mimeMessage)
        } catch (addressException: AddressException) {
            ChannelMessageResponse(RestStatus.BAD_REQUEST, "recipient parsing failed with status:${addressException.message}")
        } catch (messagingException: MessagingException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${messagingException.message}")
        } catch (ioException: IOException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${ioException.message}")
        }
    }

    /**
     * Sending mime message over Amazon SES.
     * @param refTag ref tag for logging purpose
     * @param mimeMessage mime message to send to Amazon SES
     * @return Channel message response
     */
    private fun sendMimeMessage(refTag: String, mimeMessage: MimeMessage): ChannelMessageResponse {
        return try {
            log.info("Sending Email-SES:$refTag")
            val region = Region.US_WEST_2
            val client = SecurityAccess.doPrivileged {
                SesClient.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build()
            }
            val outputStream = ByteArrayOutputStream()
            mimeMessage.writeTo(outputStream)
            val data = SdkBytes.fromByteArray(outputStream.toByteArray())
            val rawMessage = RawMessage.builder()
                .data(data)
                .build()
            val rawEmailRequest = SendRawEmailRequest.builder()
                .rawMessage(rawMessage)
                .build()
            val response = SecurityAccess.doPrivileged { client.sendRawEmail(rawEmailRequest) }
            log.info("Email-SES:$refTag status:$response")
            ChannelMessageResponse(RestStatus.OK, "Success")
        } catch (exception: MessageRejectedException) {
            ChannelMessageResponse(RestStatus.SERVICE_UNAVAILABLE, getSesExceptionText(exception))
        } catch (exception: MailFromDomainNotVerifiedException) {
            ChannelMessageResponse(RestStatus.FORBIDDEN, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetDoesNotExistException) {
            ChannelMessageResponse(RestStatus.NOT_IMPLEMENTED, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetSendingPausedException) {
            ChannelMessageResponse(RestStatus.SERVICE_UNAVAILABLE, getSesExceptionText(exception))
        } catch (exception: AccountSendingPausedException) {
            ChannelMessageResponse(RestStatus.INSUFFICIENT_STORAGE, getSesExceptionText(exception))
        } catch (exception: SesException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, getSesExceptionText(exception))
        } catch (exception: SdkException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, getSdkExceptionText(exception))
        }
    }

    /**
     * Create error string from Amazon SES Exceptions
     * @param exception SES Exception
     * @return generated error string
     */
    private fun getSesExceptionText(exception: SesException): String {
        val httpResponse = exception.awsErrorDetails().sdkHttpResponse()
        return "sendEmail error, SES status:${httpResponse.statusCode()}:${httpResponse.statusText()}"
    }

    /**
     * Create error string from Amazon SDK Exceptions
     * @param exception SDK Exception
     * @return generated error string
     */
    private fun getSdkExceptionText(exception: SdkException) = "sendEmail error, SDK status:${exception.message}"
}
