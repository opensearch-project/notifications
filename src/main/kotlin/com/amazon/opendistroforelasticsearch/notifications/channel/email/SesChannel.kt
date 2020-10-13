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

package com.amazon.opendistroforelasticsearch.notifications.channel.email

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
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
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.MimeMessage

/**
 * Notification channel for sending mail over Amazon SES.
 */
internal object SesChannel : BaseEmailChannel() {
    private val log = LogManager.getLogger(javaClass)

    /**
     * {@inheritDoc}
     */
    override fun prepareSession(refTag: String, recipient: String, channelMessage: ChannelMessage): Session {
        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        return Session.getInstance(prop)
    }

    /**
     * {@inheritDoc}
     */
    override fun sendMimeMessage(refTag: String, mimeMessage: MimeMessage): ChannelMessageResponse {
        return try {
            log.debug("$PLUGIN_NAME:Sending Email-SES:$refTag")
            val region = Region.of(PluginSettings.sesAwsRegion)
            val client = SecurityAccess.doPrivileged {
                SesClient.builder().region(region).credentialsProvider(DefaultCredentialsProvider.create()).build()
            }
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
                log.info("$PLUGIN_NAME:Email-SES:$refTag status:$response")
                ChannelMessageResponse(RestStatus.OK, "Success")
            } else {
                ChannelMessageResponse(RestStatus.REQUEST_ENTITY_TOO_LARGE, "Email size($emailSize) larger than ${PluginSettings.emailSizeLimit}")
            }
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
        log.info("$PLUGIN_NAME:SesException $exception")
        return "sendEmail Error, SES status:${httpResponse.statusCode()}:${httpResponse.statusText()}"
    }

    /**
     * Create error string from Amazon SDK Exceptions
     * @param exception SDK Exception
     * @return generated error string
     */
    private fun getSdkExceptionText(exception: SdkException): String {
        log.info("$PLUGIN_NAME:SdkException $exception")
        return "sendEmail Error, SDK status:${exception.message}"
    }
}
