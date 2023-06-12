package org.opensearch.notifications.core.destinations

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.client.EmailMimeProvider
import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.spi.model.MessageContent
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Session

internal class EmailMimeProviderTests {
    @AfterEach
    fun reset() {
        PluginSettings.reset()
    }

    @Test
    // Use default config to sanitize html,
    // h1, p and other basic elements will be kept, images and links will be kept,
    // but script and iframe will be sanitized
    fun testPrepareMimeMessageWithDefaultHTMLSanitizationConfig() {
        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = "0.0.0.0"
        prop["mail.smtp.port"] = "587"
        val session = Session.getInstance(prop)
        val subject = "Test sending HTML email subject"
        val htmlBody = "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "<script>\n" +
            "document.getElementById(\"demo\").innerHTML = \"Test script for html sanitization\";\n" +
            "</script>\n" +
            "<a href=\"https://a.com/x\">\n" +
            "Test link for html sanitization\n" +
            "</a>\n" +
            "<iframe src=\"test iframe url\" title=\"Test iframe for html sanitization\">\n" +
            "</iframe>\n" +
            "<img src=\"x.jpg\" alt=\"Test image for html sanitization\">\n"

        val message = MessageContent(subject, "", htmlBody)
        val sanitizedMimeMessage =
            EmailMimeProvider.prepareMimeMessage(session, "from@from.com", "recipient@recipient.com", message)
        val outputStream = ByteArrayOutputStream()
        sanitizedMimeMessage.writeTo(outputStream)
        val sanitizedMessageStr = outputStream.toString()

        Assertions.assertTrue(sanitizedMessageStr.contains("Test sending HTML email body"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Hello OpenSearch."))
        Assertions.assertTrue(sanitizedMessageStr.contains("<img src="))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test image for html sanitization"))
        Assertions.assertTrue(sanitizedMessageStr.contains("<a href="))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test link for html sanitization"))

        Assertions.assertFalse(sanitizedMessageStr.contains("<script>"))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test script for html sanitization"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<iframe src="))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test iframe for html sanitization"))
    }

    @Test
    // Test prepare mime message without html sanitization
    fun testPrepareMimeMessageWithoutHTMLSanitization() {
        PluginSettings.enableEmailHtmlSanitization = false

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = "0.0.0.0"
        prop["mail.smtp.port"] = "587"
        val session = Session.getInstance(prop)
        val subject = "Test sending HTML email subject"
        val htmlBody = "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "<script>\n" +
            "document.getElementById(\"demo\").innerHTML = \"Test script for html sanitization\";\n" +
            "</script>\n" +
            "<a href=\"https://a.com/x\">\n" +
            "Test link for html sanitization\n" +
            "</a>\n" +
            "<iframe src=\"test iframe url\" title=\"Test iframe for html sanitization\">\n" +
            "</iframe>\n" +
            "<img src=\"x.jpg\" alt=\"Test image for html sanitization\">\n"

        val message = MessageContent(subject, "", htmlBody)
        val sanitizedMimeMessage =
            EmailMimeProvider.prepareMimeMessage(session, "from@from.com", "recipient@recipient.com", message)
        val outputStream = ByteArrayOutputStream()
        sanitizedMimeMessage.writeTo(outputStream)
        val sanitizedMessageStr = outputStream.toString()

        Assertions.assertTrue(sanitizedMessageStr.contains("Test sending HTML email body"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Hello OpenSearch."))
        Assertions.assertTrue(sanitizedMessageStr.contains("<img src="))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test image for html sanitization"))
        Assertions.assertTrue(sanitizedMessageStr.contains("<a href="))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test link for html sanitization"))

        Assertions.assertTrue(sanitizedMessageStr.contains("<script>"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test script for html sanitization"))
        Assertions.assertTrue(sanitizedMessageStr.contains("<iframe src="))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test iframe for html sanitization"))
    }

    @Test
    // Test prepare mime message with custom html sanitization allow list
    fun testPrepareMimeMessageWithCustomHtmlSanitizationAllowList() {
        PluginSettings.emailHtmlSanitizationAllowList = listOf("h1", "p")

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = "0.0.0.0"
        prop["mail.smtp.port"] = "587"
        val session = Session.getInstance(prop)
        val subject = "Test sending HTML email subject"
        val htmlBody = "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "<script>\n" +
            "document.getElementById(\"demo\").innerHTML = \"Test script for html sanitization\";\n" +
            "</script>\n" +
            "<a href=\"https://a.com/x\">\n" +
            "Test link for html sanitization\n" +
            "</a>\n" +
            "<iframe src=\"test iframe url\" title=\"Test iframe for html sanitization\">\n" +
            "</iframe>\n" +
            "<img src=\"x.jpg\" alt=\"Test image for html sanitization\">\n"

        val message = MessageContent(subject, "", htmlBody)
        val sanitizedMimeMessage =
            EmailMimeProvider.prepareMimeMessage(session, "from@from.com", "recipient@recipient.com", message)
        val outputStream = ByteArrayOutputStream()
        sanitizedMimeMessage.writeTo(outputStream)
        val sanitizedMessageStr = outputStream.toString()

        Assertions.assertTrue(sanitizedMessageStr.contains("Test sending HTML email body"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Hello OpenSearch."))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test link for html sanitization"))

        Assertions.assertFalse(sanitizedMessageStr.contains("<img src="))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test image for html sanitization"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<a href="))
        Assertions.assertFalse(sanitizedMessageStr.contains("<script>"))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test script for html sanitization"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<iframe src="))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test iframe for html sanitization"))
    }
    @Test
    // Test prepare mime message with custom html sanitization deny list
    fun testPrepareMimeMessageWithCustomHtmlSanitizationDenyList() {
        PluginSettings.emailHtmlSanitizationAllowList = listOf("blocks_group", "links_group", "images_group")
        PluginSettings.emailHtmlSanitizationDenyList = listOf("h2", "h3")

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = "0.0.0.0"
        prop["mail.smtp.port"] = "587"
        val session = Session.getInstance(prop)
        val subject = "Test sending HTML email subject"
        val htmlBody = "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "<h2>test head2</h2>\n" +
            "<h3>test head3</h3>\n" +
            "<a href=\"https://a.com/x\">\n" +
            "Test link for html sanitization\n" +
            "</a>\n" +
            "<img src=\"x.jpg\" alt=\"Test image for html sanitization\">\n"

        val message = MessageContent(subject, "", htmlBody)
        val sanitizedMimeMessage =
            EmailMimeProvider.prepareMimeMessage(session, "from@from.com", "recipient@recipient.com", message)
        val outputStream = ByteArrayOutputStream()
        sanitizedMimeMessage.writeTo(outputStream)
        val sanitizedMessageStr = outputStream.toString()

        Assertions.assertTrue(sanitizedMessageStr.contains("<h1>"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Test sending HTML email body"))
        Assertions.assertTrue(sanitizedMessageStr.contains("<p>"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Hello OpenSearch."))
        Assertions.assertTrue(sanitizedMessageStr.contains("<a href="))
        Assertions.assertTrue(sanitizedMessageStr.contains("<img src="))
        Assertions.assertTrue(sanitizedMessageStr.contains("test head2"))
        Assertions.assertTrue(sanitizedMessageStr.contains("test head3"))

        Assertions.assertFalse(sanitizedMessageStr.contains("<h2>"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<h3>"))
    }

    @Test
    // Test prepare mime message with empty html sanitization allow list and deny list
    fun testPrepareMimeMessageWithEmptyHtmlSanitizationDenyList() {
        PluginSettings.emailHtmlSanitizationAllowList = emptyList()
        PluginSettings.emailHtmlSanitizationDenyList = emptyList()

        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = "0.0.0.0"
        prop["mail.smtp.port"] = "587"
        val session = Session.getInstance(prop)
        val subject = "Test sending HTML email subject"
        val htmlBody = "\n" +
            "<h1>Test sending HTML email body</h1>\n" +
            "<p>Hello OpenSearch.</p>\n" +
            "<script>\n" +
            "document.getElementById(\"demo\").innerHTML = \"Test script for html sanitization\";\n" +
            "</script>\n" +
            "<a href=\"https://a.com/x\">\n" +
            "Test link for html sanitization\n" +
            "</a>\n" +
            "<iframe src=\"test iframe url\" title=\"Test iframe for html sanitization\">\n" +
            "</iframe>\n" +
            "<img src=\"x.jpg\" alt=\"Test image for html sanitization\">\n"

        val message = MessageContent(subject, "", htmlBody)
        val sanitizedMimeMessage =
            EmailMimeProvider.prepareMimeMessage(session, "from@from.com", "recipient@recipient.com", message)
        val outputStream = ByteArrayOutputStream()
        sanitizedMimeMessage.writeTo(outputStream)
        val sanitizedMessageStr = outputStream.toString()

        Assertions.assertTrue(sanitizedMessageStr.contains("Test sending HTML email body"))
        Assertions.assertTrue(sanitizedMessageStr.contains("Hello OpenSearch."))

        Assertions.assertFalse(sanitizedMessageStr.contains("<img src="))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test image for html sanitization"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<a href="))
        Assertions.assertFalse(sanitizedMessageStr.contains("<script>"))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test script for html sanitization"))
        Assertions.assertFalse(sanitizedMessageStr.contains("<iframe src="))
        Assertions.assertFalse(sanitizedMessageStr.contains("Test iframe for html sanitization"))
    }
}
