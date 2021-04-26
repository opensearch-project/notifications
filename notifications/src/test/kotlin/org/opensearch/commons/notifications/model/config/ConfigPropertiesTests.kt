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

internal class ConfigPropertiesTests {


    @Test
    fun `Validate config property reader slack`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.Slack)!!.getConfigDataReader(), Slack.reader)
    }

    @Test
    fun `Validate config data parse  slack`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleSlack)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .Slack)!!.createConfigData(it.map()) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Validate config property reader chime`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.Chime)!!.getConfigDataReader(), Chime.reader)
    }

    @Test
    fun `Validate config data parse chime`() {
        val sampleChime = Chime("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleChime)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .Chime)!!.createConfigData(it.map()) }
        assertEquals(sampleChime, recreatedObject)
    }

    @Test
    fun `Validate config property reader webhook`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.Webhook)!!.getConfigDataReader(), Webhook.reader)
    }

    @Test
    fun `Validate config data parse webhook`() {
        val sampleWebhook = Webhook("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleWebhook)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .Webhook)!!.createConfigData(it.map()) }
        assertEquals(sampleWebhook, recreatedObject)
    }

    @Test
    fun `Validate config property reader email`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.Email)!!.getConfigDataReader(), Email.reader)
    }


    @Test
    fun `Validate config data parse email`() {
        val sampleEmail = Email(
                "sampleAccountId",
                listOf("email1@email.com", "email2@email.com"),
                listOf("sample_group_id_1", "sample_group_id_2")
        )
        val jsonString = getJsonString(sampleEmail)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .Email)!!.createConfigData(it.map()) }
        assertEquals(sampleEmail, recreatedObject)
    }

    @Test
    fun `Validate config property reader EmailGroup`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.EmailGroup)!!.getConfigDataReader(), EmailGroup.reader)
    }

    @Test
    fun `Validate config data parse EmailGroup`() {
        val sampleEmailGroup = EmailGroup(listOf("email1@email.com", "email2@email.com"))
        val jsonString = getJsonString(sampleEmailGroup)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .EmailGroup)!!.createConfigData(it.map()) }
        assertEquals(sampleEmailGroup, recreatedObject)
    }

    @Test
    fun `Validate config property reader SmtpAccount`() {
        assertEquals(CONFIG_TYPE_VS_PROPERTIES.get(ConfigType.SmtpAccount)!!.getConfigDataReader(), EmailGroup.reader)
    }

    @Test
    fun `Validate config data parse SmtpAccount`() {
        val sampleSmtpAccount = SmtpAccount("domain.com", 1234, SmtpAccount.MethodType.Ssl, "from@domain.com")
        val jsonString = getJsonString(sampleSmtpAccount)
        val recreatedObject = createObjectFromJsonString(jsonString) { CONFIG_TYPE_VS_PROPERTIES.get(ConfigType
                .SmtpAccount)!!.createConfigData(it.map()) }
        assertEquals(sampleSmtpAccount, recreatedObject)
    }
}
