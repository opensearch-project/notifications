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

import com.sun.mail.util.MailConnectException
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.utils.logger
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.model.ChannelMessageResponse
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.spi.utils.SecurityAccess
import org.opensearch.rest.RestStatus
import java.util.Properties
import javax.mail.MessagingException
import javax.mail.SendFailedException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.MimeMessage

/**
 * Notification channel for sending mail over SMTP server.
 */
internal object SmtpChannel : BaseEmailChannel() {
    private val log by logger(javaClass)

    /**
     * {@inheritDoc}
     */
    override fun prepareSession(refTag: String, recipient: String, channelMessage: ChannelMessage): Session {
        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = PluginSettings.smtpHost
        prop["mail.smtp.port"] = PluginSettings.smtpPort
        when (PluginSettings.smtpTransportMethod) {
            "ssl" -> prop["mail.smtp.ssl.enable"] = true
            "starttls" -> prop["mail.smtp.starttls.enable"] = true
        }
        return Session.getInstance(prop)
    }

    /**
     * {@inheritDoc}
     */
    override fun sendMimeMessage(refTag: String, recipient: String, mimeMessage: MimeMessage): ChannelMessageResponse {
        return try {
            log.debug("$LOG_PREFIX:Sending Email-SMTP:$refTag")
            SecurityAccess.doPrivileged { Transport.send(mimeMessage) }
            log.info("$LOG_PREFIX:Email-SMTP:$refTag sent")
            ChannelMessageResponse(recipient, RestStatus.OK, "Success")
        } catch (exception: SendFailedException) {
            ChannelMessageResponse(recipient, RestStatus.BAD_GATEWAY, getMessagingExceptionText(exception))
        } catch (exception: MailConnectException) {
            ChannelMessageResponse(recipient, RestStatus.SERVICE_UNAVAILABLE, getMessagingExceptionText(exception))
        } catch (exception: MessagingException) {
            ChannelMessageResponse(recipient, RestStatus.FAILED_DEPENDENCY, getMessagingExceptionText(exception))
        }
    }

    /**
     * Create error string from MessagingException
     * @param exception Messaging Exception
     * @return generated error string
     */
    private fun getMessagingExceptionText(exception: MessagingException): String {
        log.info("$LOG_PREFIX:EmailException $exception")
        return "sendEmail Error, status:${exception.message}"
    }
}
