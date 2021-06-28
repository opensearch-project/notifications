/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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

package org.opensearch.notifications.channel.email

import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.model.ChannelMessageResponse
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.spi.utils.SecurityAccess
import org.opensearch.rest.RestStatus
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
    private val log by logger(javaClass)

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
    override fun sendMimeMessage(refTag: String, recipient: String, mimeMessage: MimeMessage): ChannelMessageResponse {
        return try {
            log.debug("$LOG_PREFIX:Sending Email-SES:$refTag")
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
                log.info("$LOG_PREFIX:Email-SES:$refTag status:$response")
                ChannelMessageResponse(recipient, RestStatus.OK, "Success")
            } else {
                ChannelMessageResponse(
                    recipient,
                    RestStatus.REQUEST_ENTITY_TOO_LARGE,
                    "Email size($emailSize) larger than ${PluginSettings.emailSizeLimit}"
                )
            }
        } catch (exception: MessageRejectedException) {
            ChannelMessageResponse(recipient, RestStatus.SERVICE_UNAVAILABLE, getSesExceptionText(exception))
        } catch (exception: MailFromDomainNotVerifiedException) {
            ChannelMessageResponse(recipient, RestStatus.FORBIDDEN, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetDoesNotExistException) {
            ChannelMessageResponse(recipient, RestStatus.NOT_IMPLEMENTED, getSesExceptionText(exception))
        } catch (exception: ConfigurationSetSendingPausedException) {
            ChannelMessageResponse(recipient, RestStatus.SERVICE_UNAVAILABLE, getSesExceptionText(exception))
        } catch (exception: AccountSendingPausedException) {
            ChannelMessageResponse(recipient, RestStatus.INSUFFICIENT_STORAGE, getSesExceptionText(exception))
        } catch (exception: SesException) {
            ChannelMessageResponse(recipient, RestStatus.FAILED_DEPENDENCY, getSesExceptionText(exception))
        } catch (exception: SdkException) {
            ChannelMessageResponse(recipient, RestStatus.FAILED_DEPENDENCY, getSdkExceptionText(exception))
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
