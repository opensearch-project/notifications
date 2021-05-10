package org.opensearch.commons.notifications.model.config

import org.junit.jupiter.api.Test
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ConfigPropertiesTests {
    @Test
    fun `Validate config property reader slack`() {
        assertEquals(ConfigPropertiesUtils.getReaderForConfigType(ConfigType.Slack), Slack.reader)
    }

    @Test
    fun `Validate tag for slack`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("slack"))
    }

    @Test
    fun `Validate config type for tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("slack"), ConfigType.Slack)
    }

    @Test
    fun `Validate tag for given config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.Slack), "slack")
    }

    @Test
    fun `Validate config property reader chime`() {
        assertEquals(
            ConfigPropertiesUtils.getReaderForConfigType(ConfigType.Chime)!!, Chime
            .reader
        )
    }

    @Test
    fun `Validate tag for chime`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("chime"))
    }

    @Test
    fun `Validate config type for chime tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("chime"), ConfigType.Chime)
    }

    @Test
    fun `Validate tag for given chime config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.Chime), "chime")
    }

    @Test
    fun `Validate config property reader webhook`() {
        assertEquals(
            ConfigPropertiesUtils.getReaderForConfigType(ConfigType.Webhook),
            Webhook.reader
        )
    }

    @Test
    fun `Validate tag for webhook`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("webhook"))
    }

    @Test
    fun `Validate config type for webhook tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("webhook"), ConfigType.Webhook)
    }

    @Test
    fun `Validate tag for given webhook config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.Webhook), "webhook")
    }

    @Test
    fun `Validate config property reader email`() {
        assertEquals(
            ConfigPropertiesUtils.getReaderForConfigType(ConfigType.Email), Email
            .reader
        )
    }

    @Test
    fun `Validate tag for email`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("email"))
    }

    @Test
    fun `Validate config type for email tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("email"), ConfigType.Email)
    }

    @Test
    fun `Validate tag for given email config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.Email), "email")
    }

    @Test
    fun `Validate config property reader EmailGroup`() {
        assertEquals(
            ConfigPropertiesUtils.getReaderForConfigType(ConfigType.EmailGroup),
            EmailGroup.reader
        )
    }

    @Test
    fun `Validate tag for emailGroup`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("email_group"))
    }

    @Test
    fun `Validate config type for emailGroup tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("email_group"), ConfigType.EmailGroup)
    }

    @Test
    fun `Validate tag for given emailGroup config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.EmailGroup), "email_group")
    }

    @Test
    fun `Validate config property reader SmtpAccount`() {
        assertEquals(
            ConfigPropertiesUtils.getReaderForConfigType(ConfigType.SmtpAccount),
            SmtpAccount.reader
        )
    }

    @Test
    fun `Validate tag for smtmAccount`() {
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("smtp_account"))
    }

    @Test
    fun `Validate config type for smtpAccount tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("smtp_account"), ConfigType.SmtpAccount)
    }

    @Test
    fun `Validate tag for given smtpAccount config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.SmtpAccount), "smtp_account")
    }

    @Test
    fun `Validate config data parse  slack`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleSlack)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            ConfigPropertiesUtils.createConfigData(ConfigType
                .Slack, it)
        }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Validate config data parse chime`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleChime)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            ConfigPropertiesUtils.createConfigData(ConfigType
                .Chime, it)
        }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Validate config data parse webhook`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleWebhook)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            ConfigPropertiesUtils.createConfigData(ConfigType
                .Webhook, it)
        }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Validate config data parse EmailGroup`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = getJsonString(sampleEmailGroup)
        val recreatedObject =
            createObjectFromJsonString(jsonString) { ConfigPropertiesUtils.createConfigData(ConfigType.EmailGroup, it) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `Validate config data parse SmtpAccount`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            ConfigPropertiesUtils.createConfigData(ConfigType.SmtpAccount, it
            )
        }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }
}
