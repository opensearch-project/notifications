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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.notifications.core.client

import org.opensearch.notifications.core.spi.model.MessageContent
import java.util.Base64
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

/**
 * Object for creating mime mimeMessage from the channel mimeMessage for sending mail.
 */
internal object EmailMimeProvider {
    /**
     * Create and prepare mime mimeMessage to send mail
     * @param session The mail session to use to create mime mimeMessage
     * @param fromAddress The sender email address
     * @param recipient The recipient email address
     * @param messageContent The mimeMessage to send notification
     * @return The created and prepared mime mimeMessage object
     */
    fun prepareMimeMessage(
        session: Session,
        fromAddress: String,
        recipient: String,
        messageContent: MessageContent
    ): MimeMessage {
        // Create a new MimeMessage object
        val mimeMessage = MimeMessage(session)

        // Add from:
        mimeMessage.setFrom(fromAddress)

        // Add to:
        mimeMessage.setRecipients(Message.RecipientType.TO, recipient)

        // Add Subject:
        mimeMessage.setSubject(messageContent.title, "UTF-8")

        // Create a multipart/alternative child container
        val msgBody = MimeMultipart("alternative")

        // Create a wrapper for the HTML and text parts
        val bodyWrapper = MimeBodyPart()

        // Define the text part (if html part does not exists then use "-" string
        val textPart = MimeBodyPart()
        textPart.setContent(messageContent.textDescription, "text/plain; charset=UTF-8")
        // Add the text part to the child container
        msgBody.addBodyPart(textPart)

        // Define the HTML part
        if (messageContent.htmlDescription != null) {
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(messageContent.htmlDescription, "text/html; charset=UTF-8")
            // Add the HTML part to the child container
            msgBody.addBodyPart(htmlPart)
        }
        // Add the child container to the wrapper object
        bodyWrapper.setContent(msgBody)

        // Create a multipart/mixed parent container
        val msg = MimeMultipart("mixed")

        // Add the parent container to the mimeMessage
        mimeMessage.setContent(msg)

        // Add the multipart/alternative part to the mimeMessage
        msg.addBodyPart(bodyWrapper)

        @SuppressWarnings("ComplexCondition")
        if (messageContent.fileName != null &&
            messageContent.fileData != null &&
            messageContent.fileContentType != null &&
            messageContent.fileEncoding != null
        ) {
            // Add the attachment to the mimeMessage
            var attachmentMime: MimeBodyPart? = null
            when (messageContent.fileEncoding) {
                "text" -> attachmentMime = createTextAttachmentPart(messageContent)
                "base64" -> attachmentMime = createBinaryAttachmentPart(messageContent)
            }
            if (attachmentMime != null) {
                msg.addBodyPart(attachmentMime)
            }
        }

        return mimeMessage
    }

    /**
     * Create a binary attachment part from channel attachment mimeMessage
     * @param messageContent channel attachment mimeMessage
     * @return created mime body part for binary attachment
     */
    private fun createBinaryAttachmentPart(messageContent: MessageContent): MimeBodyPart {
        val attachmentMime = MimeBodyPart()
        val fds = ByteArrayDataSource(
            Base64.getMimeDecoder().decode(messageContent.fileData),
            messageContent.fileContentType ?: "application/octet-stream"
        )
        attachmentMime.dataHandler = DataHandler(fds)
        attachmentMime.fileName = messageContent.fileName
        return attachmentMime
    }

    /**
     * Create a text attachment part from channel attachment mimeMessage
     * @param messageContent channel attachment mimeMessage
     * @return created mime body part for text attachment
     */
    private fun createTextAttachmentPart(messageContent: MessageContent): MimeBodyPart {
        val attachmentMime = MimeBodyPart()
        val subContentType = messageContent.fileContentType?.substringAfterLast('/') ?: "plain"
        attachmentMime.setText(messageContent.fileData, "UTF-8", subContentType)
        attachmentMime.fileName = messageContent.fileName
        return attachmentMime
    }
}
