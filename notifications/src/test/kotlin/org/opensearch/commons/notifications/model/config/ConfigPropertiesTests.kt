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
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("emailGroup"))
    }

    @Test
    fun `Validate config type for emailGroup tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("emailGroup"), ConfigType.EmailGroup)
    }

    @Test
    fun `Validate tag for given emailGroup config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.EmailGroup), "emailGroup")
    }

    @Test
    fun `Validate config data parse EmailGroup`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = getJsonString(sampleEmailGroup)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            CONFIG_TYPE_VS_PROPERTIES.get(
                ConfigType
                    .EmailGroup
            )!!.createConfigData(it.map())
        }
        assertEquals(sampleEmailGroup, recreatedObject)
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
        assertTrue(ConfigPropertiesUtils.isValidConfigTag("smtpAccount"))
    }

    @Test
    fun `Validate config type for smtpAccount tag`() {
        assertEquals(ConfigPropertiesUtils.getConfigTypeForTag("smtpAccount"), ConfigType.SmtpAccount)
    }

    @Test
    fun `Validate tag for given smtpAccount config type`() {
        assertEquals(ConfigPropertiesUtils.getTagForConfigType(ConfigType.SmtpAccount), "smtpAccount")
    }
}
