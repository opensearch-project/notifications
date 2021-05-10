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

package org.opensearch.notifications

import com.google.gson.JsonObject
import org.junit.Assert
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook

fun verifyEquals(slack: Slack, jsonObject: JsonObject) {
    Assert.assertEquals(slack.url, jsonObject.get("url").asString)
}

fun verifyEquals(chime: Chime, jsonObject: JsonObject) {
    Assert.assertEquals(chime.url, jsonObject.get("url").asString)
}

fun verifyEquals(webhook: Webhook, jsonObject: JsonObject) {
    Assert.assertEquals(webhook.url, jsonObject.get("url").asString)
}

fun verifyEquals(email: Email, jsonObject: JsonObject) {
    Assert.assertEquals(email.emailAccountID, jsonObject.get("email_account_id").asString)
    val defaultRecipients = jsonObject.get("default_recipients").asJsonArray
    Assert.assertEquals(email.defaultRecipients.size, defaultRecipients.size())
    defaultRecipients.forEach { email.defaultRecipients.contains(it.asString) }
    val defaultEmailGroupIds = jsonObject.get("default_email_group_ids").asJsonArray
    Assert.assertEquals(email.defaultEmailGroupIds.size, defaultEmailGroupIds.size())
    defaultEmailGroupIds.forEach { email.defaultEmailGroupIds.contains(it.asString) }
}

fun verifyEquals(emailGroup: EmailGroup, jsonObject: JsonObject) {
    val recipients = jsonObject.get("recipients").asJsonArray
    Assert.assertEquals(emailGroup.recipients.size, recipients.size())
    recipients.forEach { emailGroup.recipients.contains(it.asString) }
}

fun verifyEquals(smtpAccount: SmtpAccount, jsonObject: JsonObject) {
    Assert.assertEquals(smtpAccount.host, jsonObject.get("host").asString)
    Assert.assertEquals(smtpAccount.port, jsonObject.get("port").asInt)
    Assert.assertEquals(smtpAccount.method, SmtpAccount.MethodType.valueOf(jsonObject.get("method").asString))
    Assert.assertEquals(smtpAccount.fromAddress, jsonObject.get("from_address").asString)
    // TODO: Validate username,password?
}

fun verifyEquals(config: NotificationConfig, jsonObject: JsonObject) {
    Assert.assertEquals(config.name, jsonObject.get("name").asString)
    Assert.assertEquals(config.description, jsonObject.get("description").asString)
    Assert.assertEquals(config.configType.name, jsonObject.get("config_type").asString)
    Assert.assertEquals(config.isEnabled, jsonObject.get("is_enabled").asBoolean)
    val features = jsonObject.get("features").asJsonArray
    Assert.assertEquals(config.features.size, features.size())
    features.forEach { config.features.contains(Feature.valueOf(it.asString)) }
    when (config.configType) {
        ConfigType.Slack -> verifyEquals((config.configData as Slack), jsonObject.get("slack").asJsonObject)
        ConfigType.Chime -> verifyEquals((config.configData as Chime), jsonObject.get("chime").asJsonObject)
        ConfigType.Webhook -> verifyEquals((config.configData as Webhook ), jsonObject.get("webhook").asJsonObject)
        ConfigType.Email -> verifyEquals((config.configData as Email), jsonObject.get("email").asJsonObject)
        ConfigType.SmtpAccount -> verifyEquals((config.configData as SmtpAccount), jsonObject.get("smtp_account")
            .asJsonObject)
        ConfigType.EmailGroup -> verifyEquals((config.configData as EmailGroup), jsonObject.get("email_group")
            .asJsonObject)
        else -> Assert.fail("configType:${config.configType} not handled in test")
    }
}

fun verifySingleConfigEquals(configId: String, config: NotificationConfig, jsonObject: JsonObject) {
    Assert.assertEquals(1, jsonObject.get("total_hits").asInt)
    val getResponseItem = jsonObject.get("notification_config_list").asJsonArray[0].asJsonObject
    Assert.assertEquals(configId, getResponseItem.get("config_id").asString)
    Assert.assertEquals("", getResponseItem.get("tenant").asString)
    verifyEquals(config, getResponseItem.get("notification_config").asJsonObject)
}

fun verifyMultiConfigEquals(objectMap: Map<String, NotificationConfig>, jsonObject: JsonObject) {
    Assert.assertEquals(objectMap.size, jsonObject.get("total_hits").asInt)
    val items = jsonObject.get("notification_config_list").asJsonArray
    items.forEach {
        val item = it.asJsonObject
        val configId = item.get("config_id").asString
        Assert.assertNotNull(configId)
        val config = objectMap[configId]
        Assert.assertNotNull(config)
        Assert.assertEquals("", item.get("tenant").asString)
        verifyEquals(config!!, item.get("notification_config").asJsonObject)
    }
}
