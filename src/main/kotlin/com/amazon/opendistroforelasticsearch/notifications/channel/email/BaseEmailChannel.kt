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

import com.amazon.opendistroforelasticsearch.notifications.channel.NotificationChannel
import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessage
import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessageResponse
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import com.amazon.opendistroforelasticsearch.notifications.throttle.Counters
import org.elasticsearch.rest.RestStatus
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.AddressException
import javax.mail.internet.MimeMessage

/**
 * Notification channel for sending mail to Email server.
 */
internal abstract class BaseEmailChannel : NotificationChannel {

    companion object {
        private const val MINIMUM_EMAIL_HEADER_LENGTH = 160 // minimum value from 100 reference emails
    }

    /**
     * {@inheritDoc}
     */
    override fun updateCounter(refTag: String, recipient: String, channelMessage: ChannelMessage, counter: Counters) {
        counter.emailSentSuccessCount.incrementAndGet()
    }

    /**
     * {@inheritDoc}
     */
    override fun sendMessage(refTag: String, recipient: String, channelMessage: ChannelMessage, counter: Counters): ChannelMessageResponse {
        val retStatus = sendEmail(refTag, recipient, channelMessage)
        if (retStatus.statusCode == RestStatus.OK) {
            counter.emailSentSuccessCount.incrementAndGet()
        } else {
            counter.emailSentFailureCount.incrementAndGet()
        }
        return retStatus
    }

    /**
     * Sending Email message to server.
     * @param refTag ref tag for logging purpose
     * @param recipient email recipient to send mail to
     * @param channelMessage email message information to compose email
     * @return Channel message response
     */
    private fun sendEmail(refTag: String, recipient: String, channelMessage: ChannelMessage): ChannelMessageResponse {
        val fromAddress = PluginSettings.emailFromAddress
        if (PluginSettings.UNCONFIGURED_EMAIL_ADDRESS == fromAddress) {
            return ChannelMessageResponse(RestStatus.NOT_IMPLEMENTED, "Email from: address not configured")
        }
        if (isMessageSizeOverLimit(channelMessage)) {
            return ChannelMessageResponse(RestStatus.REQUEST_ENTITY_TOO_LARGE, "Email size larger than ${PluginSettings.emailSizeLimit}")
        }
        val mimeMessage: MimeMessage
        return try {
            val session = prepareSession(refTag, recipient, channelMessage)
            mimeMessage = EmailMimeProvider.prepareMimeMessage(session, fromAddress, recipient, channelMessage)
            sendMimeMessage(refTag, mimeMessage)
        } catch (addressException: AddressException) {
            ChannelMessageResponse(RestStatus.BAD_REQUEST, "recipient parsing failed with status:${addressException.message}")
        } catch (messagingException: MessagingException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${messagingException.message}")
        } catch (ioException: IOException) {
            ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${ioException.message}")
        }
    }

    private fun isMessageSizeOverLimit(channelMessage: ChannelMessage): Boolean {
        val approxAttachmentLength = if (channelMessage.attachment != null) {
            MINIMUM_EMAIL_HEADER_LENGTH +
                channelMessage.attachment.fileData.length +
                channelMessage.attachment.fileName.length
        } else {
            0
        }
        val approxEmailLength = MINIMUM_EMAIL_HEADER_LENGTH +
            channelMessage.title.length +
            channelMessage.textDescription.length +
            (channelMessage.htmlDescription?.length ?: 0) +
            approxAttachmentLength
        return approxEmailLength > PluginSettings.emailSizeLimit
    }

    /**
     * Prepare Session for creating Email mime message.
     * @param refTag ref tag for logging purpose
     * @param recipient email recipient to send mail to
     * @param channelMessage email message information to compose email
     * @return initialized/prepared Session for creating mime message
     */
    protected abstract fun prepareSession(refTag: String, recipient: String, channelMessage: ChannelMessage): Session

    /**
     * Sending Email mime message to server.
     * @param refTag ref tag for logging purpose
     * @param mimeMessage mime message to send to Email server
     * @return Channel message response
     */
    protected abstract fun sendMimeMessage(refTag: String, mimeMessage: MimeMessage): ChannelMessageResponse
}
