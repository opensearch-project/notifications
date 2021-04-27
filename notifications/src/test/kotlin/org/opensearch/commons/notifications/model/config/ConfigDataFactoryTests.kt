package org.opensearch.commons.notifications.model.config

import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import kotlin.test.assertEquals
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.Webhook
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.SmtpAccount

internal class ConfigDataFactoryTests {

    @Test
    fun `Validate config data parse  slack`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleSlack)
        val recreatedObject = createObjectFromJsonString(jsonString) { createConfigData(ConfigType.Slack, it.map()) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Validate config data parse chime`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleChime)
        val recreatedObject = createObjectFromJsonString(jsonString) { createConfigData(ConfigType.Chime, it.map()) }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Validate config data parse webhook`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleWebhook)
        val recreatedObject = createObjectFromJsonString(jsonString) { createConfigData(ConfigType.Webhook, it.map()) }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Validate config data parse EmailGroup`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = getJsonString(sampleEmailGroup)
        val recreatedObject =
            createObjectFromJsonString(jsonString) { createConfigData(ConfigType.EmailGroup, it.map()) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `Validate config data parse SmtpAccount`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) {
            createConfigData(
                ConfigType.SmtpAccount, it.map
                    ()
            )
        }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }
}
