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

package com.amazon.opendistroforelasticsearch.notification.channel

import com.amazon.opendistroforelasticsearch.notification.core.ChannelMessage
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.PreencodedMimeBodyPart

object EmailMimeProvider {
    fun prepareMimeMessage(fromAddress: String, recipient: String, channelMessage: ChannelMessage): MimeMessage {
        val prop = Properties()
        prop.put("mail.transport.protocol", "smtp")
        val session = Session.getInstance(prop)

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
                "text" -> attachmentMime = getTextAttachmentPart(channelMessage.attachment)
                "base64" -> attachmentMime = getBinaryAttachmentPart(channelMessage.attachment)
            }
            if (attachmentMime != null) {
                msg.addBodyPart(attachmentMime)
            }
        }
        return message
    }

    private fun extractEmail(recipient: String): String {
        if (recipient.startsWith(EmailFactory.EMAIL_PREFIX)) {
            return recipient.drop(EmailFactory.EMAIL_PREFIX.length)
        }
        return recipient
    }

    private fun getBinaryAttachmentPart(attachment: ChannelMessage.Attachment): MimeBodyPart {
        val attachmentMime: MimeBodyPart = PreencodedMimeBodyPart("base64")
        attachmentMime.setContent(attachment.fileData, attachment.fileContentType ?: "application/octet-stream")
        attachmentMime.fileName = attachment.fileName
        return attachmentMime
    }

    private fun getTextAttachmentPart(attachment: ChannelMessage.Attachment): MimeBodyPart {
        val attachmentMime = MimeBodyPart()
        attachmentMime.setText(attachment.fileData, "UTF-8", attachment.fileContentType)
        attachmentMime.fileName = attachment.fileName
        return attachmentMime
    }
}
