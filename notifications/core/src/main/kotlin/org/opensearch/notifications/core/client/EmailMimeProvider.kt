/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.MessageContent
import org.owasp.html.AttributePolicy
import org.owasp.html.HtmlChangeListener
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
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
    private val log by logger(EmailMimeProvider::class.java)

    // org.owasp.html.Sanitizers provide some default sanitizers which can be used directly,
    // we define these html elements groups and map them to the default sanitizers, users can specify these groups in the allow list,
    // and users can still specify individual html tags or elements in the allow list or deny list
    private const val HTML_ELEMENTS_GROUP_BLOCKS = "blocks_group"
    private const val HTML_ELEMENTS_GROUP_FORMATTING = "formatting_group"
    private const val HTML_ELEMENTS_GROUP_IMAGES = "images_group"
    private const val HTML_ELEMENTS_GROUP_LINKS = "links_group"
    private const val HTML_ELEMENTS_GROUP_STYLES = "styles_group"
    private const val HTML_ELEMENTS_GROUP_TABLES = "tables_group"

    // copied from org.owasp.html.Sanitizers.INTEGER
    private val INTEGER = AttributePolicy { _, _, value ->
        val n = value.length
        if (n == 0) {
            return@AttributePolicy null
        }
        for (i in 0 until n) {
            val ch = value[i]
            if (ch == '.') {
                return@AttributePolicy if (i == 0) {
                    null
                } else value.substring(0, i)
                // truncate to integer.
            } else if (ch !in '0'..'9') {
                return@AttributePolicy null
            }
        }
        value
    }
    private val htmlSanitizationPolicy: PolicyFactory?
        get() {
            if (PluginSettings.enableEmailHtmlSanitization) {
                var policyBuilder = HtmlPolicyBuilder()
                if (PluginSettings.emailHtmlSanitizationAllowList.isNotEmpty()) {
                    PluginSettings.emailHtmlSanitizationAllowList.forEach { e ->
                        when (e) {
                            // we do not use the pre-defined sanitizers directly but copy the definition here,
                            // because found that deny list takes no effect if we use the pre-defined sanitizers
                            HTML_ELEMENTS_GROUP_BLOCKS -> policyBuilder = policyBuilder.allowCommonBlockElements()
                            HTML_ELEMENTS_GROUP_FORMATTING -> policyBuilder = policyBuilder.allowCommonInlineFormattingElements()
                            HTML_ELEMENTS_GROUP_IMAGES -> policyBuilder = policyBuilder.allowUrlProtocols("http", "https").allowElements("img")
                                .allowAttributes("alt", "src").onElements("img")
                                .allowAttributes("border", "height", "width").matching(INTEGER)
                                .onElements("img")
                            HTML_ELEMENTS_GROUP_LINKS -> policyBuilder = policyBuilder.allowStandardUrlProtocols().allowElements("a")
                                .allowAttributes("href").onElements("a").requireRelNofollowOnLinks()
                            HTML_ELEMENTS_GROUP_STYLES -> policyBuilder = policyBuilder.allowStyling()
                            HTML_ELEMENTS_GROUP_TABLES -> policyBuilder = policyBuilder.allowStandardUrlProtocols()
                                .allowElements(
                                    "table", "tr", "td", "th",
                                    "colgroup", "caption", "col",
                                    "thead", "tbody", "tfoot"
                                )
                                .allowAttributes("summary").onElements("table")
                                .allowAttributes("align", "valign")
                                .onElements(
                                    "table", "tr", "td", "th",
                                    "colgroup", "col",
                                    "thead", "tbody", "tfoot"
                                )
                                .allowTextIn("table")
                            else -> policyBuilder = policyBuilder.allowElements(e)
                        }
                    }
                }

                if (PluginSettings.emailHtmlSanitizationDenyList.isNotEmpty()) {
                    // deny list only accepts individual html tags or elements
                    PluginSettings.emailHtmlSanitizationDenyList.forEach { e ->
                        policyBuilder = policyBuilder.disallowElements(e)
                    }
                }

                return policyBuilder.toFactory()
            }
            return null
        }

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
            if (PluginSettings.enableEmailHtmlSanitization && htmlSanitizationPolicy != null) {
                log.info(
                    "html sanitization for email enabled, allow list: [" + PluginSettings.emailHtmlSanitizationAllowList.joinToString() + "], deny list: [" +
                        PluginSettings.emailHtmlSanitizationDenyList.joinToString() + "], will sanitize the html body of the email from $fromAddress to $recipient"
                )
                val sanitizedHtml = htmlSanitizationPolicy!!.sanitize(
                    messageContent.htmlDescription,
                    object : HtmlChangeListener<String> {
                        override fun discardedTag(context: String?, elementName: String) {
                            log.debug("html sanitization for email, discard tag: $elementName")
                        }

                        override fun discardedAttributes(
                            context: String?,
                            elementName: String,
                            vararg attributeNames: String
                        ) {
                            log.debug("html sanitization for email, discard attributes: $attributeNames")
                        }
                    },
                    null
                )
                htmlPart.setContent(sanitizedHtml, "text/html; charset=UTF-8")
            } else {
                htmlPart.setContent(messageContent.htmlDescription, "text/html; charset=UTF-8")
            }

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
