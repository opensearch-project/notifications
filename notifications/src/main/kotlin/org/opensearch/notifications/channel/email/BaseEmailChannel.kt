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
import org.opensearch.notifications.channel.NotificationChannel
import org.opensearch.notifications.model.ChannelMessageResponse
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.throttle.Counters
import org.opensearch.rest.RestStatus
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
    override fun sendMessage(
        refTag: String,
        recipient: String,
        title: String,
        channelMessage: ChannelMessage,
        counter: Counters
    ): ChannelMessageResponse {
        val retStatus = sendEmail(refTag, recipient, title, channelMessage)
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
     * @param title email subject to send
     * @param channelMessage email message information to compose email
     * @return Channel message response
     */
    private fun sendEmail(
        refTag: String,
        recipient: String,
        title: String,
        channelMessage: ChannelMessage
    ): ChannelMessageResponse {
        val fromAddress = PluginSettings.emailFromAddress
        if (PluginSettings.UNCONFIGURED_EMAIL_ADDRESS == fromAddress) {
            return ChannelMessageResponse(recipient, RestStatus.NOT_IMPLEMENTED, "Email from: address not configured")
        }
        if (isMessageSizeOverLimit(title, channelMessage)) {
            return ChannelMessageResponse(
                recipient,
                RestStatus.REQUEST_ENTITY_TOO_LARGE,
                "Email size larger than ${PluginSettings.emailSizeLimit}"
            )
        }
        val mimeMessage: MimeMessage
        return try {
            val session = prepareSession(refTag, recipient, channelMessage)
            mimeMessage = EmailMimeProvider.prepareMimeMessage(session, fromAddress, recipient, title, channelMessage)
            sendMimeMessage(refTag, recipient, mimeMessage)
        } catch (addressException: AddressException) {
            ChannelMessageResponse(
                recipient,
                RestStatus.BAD_REQUEST,
                "recipient parsing failed with status:${addressException.message}"
            )
        } catch (messagingException: MessagingException) {
            ChannelMessageResponse(
                recipient,
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${messagingException.message}"
            )
        } catch (ioException: IOException) {
            ChannelMessageResponse(
                recipient,
                RestStatus.FAILED_DEPENDENCY,
                "Email message creation failed with status:${ioException.message}"
            )
        }
    }

    private fun isMessageSizeOverLimit(title: String, channelMessage: ChannelMessage): Boolean {
        val approxAttachmentLength = if (channelMessage.attachment != null) {
            MINIMUM_EMAIL_HEADER_LENGTH +
                channelMessage.attachment.fileData.length +
                channelMessage.attachment.fileName.length
        } else {
            0
        }
        val approxEmailLength = MINIMUM_EMAIL_HEADER_LENGTH +
            title.length +
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
     * @param recipient email recipient to send mail to
     * @param mimeMessage mime message to send to Email server
     * @return Channel message response
     */
    protected abstract fun sendMimeMessage(
        refTag: String,
        recipient: String,
        mimeMessage: MimeMessage
    ): ChannelMessageResponse
}
