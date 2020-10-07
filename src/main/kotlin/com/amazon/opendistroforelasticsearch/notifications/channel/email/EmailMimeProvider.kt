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

import com.amazon.opendistroforelasticsearch.notifications.core.ChannelMessage
import java.util.Base64
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

/**
 * Object for creating mime message from the channel message for sending mail.
 */
internal object EmailMimeProvider {
    /**
     * Create and prepare mime message to send mail
     * @param session The mail session to use to create mime message
     * @param fromAddress "From:" address of the email message
     * @param recipient "To:" address of the email message
     * @param channelMessage The message to send notification
     * @return The created and prepared mime message object
     */
    fun prepareMimeMessage(session: Session, fromAddress: String, recipient: String, channelMessage: ChannelMessage): MimeMessage {
        // Create a new MimeMessage object
        val message = MimeMessage(session)

        // Add from:
        message.setFrom(extractEmail(fromAddress))

        // Add to:
        message.setRecipients(Message.RecipientType.TO, extractEmail(recipient))

        // Add Subject:
        message.setSubject(channelMessage.title, "UTF-8")

        // Create a multipart/alternative child container
        val msgBody = MimeMultipart("alternative")

        // Create a wrapper for the HTML and text parts
        val bodyWrapper = MimeBodyPart()

        // Define the text part (if html part does not exists then use "-" string
        val textPart = MimeBodyPart()
        textPart.setContent(channelMessage.textDescription, "text/plain; charset=UTF-8")
        // Add the text part to the child container
        msgBody.addBodyPart(textPart)

        // Define the HTML part
        if (channelMessage.htmlDescription != null) {
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(channelMessage.htmlDescription, "text/html; charset=UTF-8")
            // Add the HTML part to the child container
            msgBody.addBodyPart(htmlPart)
        }
        // Add the child container to the wrapper object
        bodyWrapper.setContent(msgBody)

        // Create a multipart/mixed parent container
        val msg = MimeMultipart("mixed")

        // Add the parent container to the message
        message.setContent(msg)

        // Add the multipart/alternative part to the message
        msg.addBodyPart(bodyWrapper)

        if (channelMessage.attachment != null) {
            // Add the attachment to the message
            var attachmentMime: MimeBodyPart? = null
            when (channelMessage.attachment.fileEncoding) {
                "text" -> attachmentMime = createTextAttachmentPart(channelMessage.attachment)
                "base64" -> attachmentMime = createBinaryAttachmentPart(channelMessage.attachment)
            }
            if (attachmentMime != null) {
                msg.addBodyPart(attachmentMime)
            }
        }
        return message
    }

    /**
     * Extract email address from "mailto:email@address.com" format
     * @param recipient input email address
     * @return extracted email address
     */
    private fun extractEmail(recipient: String): String {
        if (recipient.startsWith(EmailChannelFactory.EMAIL_PREFIX)) {
            return recipient.drop(EmailChannelFactory.EMAIL_PREFIX.length)
        }
        return recipient
    }

    /**
     * Create a binary attachment part from channel attachment message
     * @param attachment channel attachment message
     * @return created mime body part for binary attachment
     */
    private fun createBinaryAttachmentPart(attachment: ChannelMessage.Attachment): MimeBodyPart {
        val attachmentMime = MimeBodyPart()
        val fds = ByteArrayDataSource(Base64.getMimeDecoder().decode(attachment.fileData),
            attachment.fileContentType ?: "application/octet-stream")
        attachmentMime.dataHandler = DataHandler(fds)
        attachmentMime.fileName = attachment.fileName
        return attachmentMime
    }

    /**
     * Create a text attachment part from channel attachment message
     * @param attachment channel attachment message
     * @return created mime body part for text attachment
     */
    private fun createTextAttachmentPart(attachment: ChannelMessage.Attachment): MimeBodyPart {
        val attachmentMime = MimeBodyPart()
        val subContentType = attachment.fileContentType?.substringAfterLast('/') ?: "plain"
        attachmentMime.setText(attachment.fileData, "UTF-8", subContentType)
        attachmentMime.fileName = attachment.fileName
        return attachmentMime
    }
}
