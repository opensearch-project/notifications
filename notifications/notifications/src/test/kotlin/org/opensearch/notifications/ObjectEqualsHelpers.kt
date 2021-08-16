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
import org.opensearch.commons.notifications.model.MethodType
import org.opensearch.commons.notifications.model.NotificationConfig
import org.opensearch.commons.notifications.model.SesAccount
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Sns
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
    val defaultRecipients = jsonObject.get("recipient_list").asJsonArray
    Assert.assertEquals(email.recipients.size, defaultRecipients.size())
    defaultRecipients.forEach { email.recipients.contains(it.asString) }
    val defaultEmailGroupIds = jsonObject.get("email_group_id_list").asJsonArray
    Assert.assertEquals(email.emailGroupIds.size, defaultEmailGroupIds.size())
    defaultEmailGroupIds.forEach { email.emailGroupIds.contains(it.asString) }
}

fun verifyEquals(emailGroup: EmailGroup, jsonObject: JsonObject) {
    val recipients = jsonObject.get("recipient_list").asJsonArray
    Assert.assertEquals(emailGroup.recipients.size, recipients.size())
    recipients.forEach { emailGroup.recipients.contains(it.asString) }
}

fun verifyEquals(smtpAccount: SmtpAccount, jsonObject: JsonObject) {
    Assert.assertEquals(smtpAccount.host, jsonObject.get("host").asString)
    Assert.assertEquals(smtpAccount.port, jsonObject.get("port").asInt)
    Assert.assertEquals(smtpAccount.method, MethodType.fromTagOrDefault(jsonObject.get("method").asString))
    Assert.assertEquals(smtpAccount.fromAddress, jsonObject.get("from_address").asString)
}

fun verifyEquals(sesAccount: SesAccount, jsonObject: JsonObject) {
    Assert.assertEquals(sesAccount.awsRegion, jsonObject.get("region").asString)
    Assert.assertEquals(sesAccount.roleArn, jsonObject.get("role_arn").asString)
    Assert.assertEquals(sesAccount.fromAddress, jsonObject.get("from_address").asString)
}

fun verifyEquals(sns: Sns, jsonObject: JsonObject) {
    Assert.assertEquals(sns.topicArn, jsonObject.get("topic_arn").asString)
    Assert.assertEquals(sns.roleArn, jsonObject.get("role_arn").asString)
}

fun verifyEquals(config: NotificationConfig, jsonObject: JsonObject) {
    Assert.assertEquals(config.name, jsonObject.get("name").asString)
    Assert.assertEquals(config.description, jsonObject.get("description").asString)
    Assert.assertEquals(config.configType.tag, jsonObject.get("config_type").asString)
    Assert.assertEquals(config.isEnabled, jsonObject.get("is_enabled").asBoolean)
    val features = jsonObject.get("feature_list").asJsonArray
    Assert.assertEquals(config.features.size, features.size())
    features.forEach { config.features.contains(it.asString) }
    when (config.configType) {
        ConfigType.SLACK -> verifyEquals((config.configData as Slack), jsonObject.get("slack").asJsonObject)
        ConfigType.CHIME -> verifyEquals((config.configData as Chime), jsonObject.get("chime").asJsonObject)
        ConfigType.WEBHOOK -> verifyEquals((config.configData as Webhook), jsonObject.get("webhook").asJsonObject)
        ConfigType.EMAIL -> verifyEquals((config.configData as Email), jsonObject.get("email").asJsonObject)
        ConfigType.SMTP_ACCOUNT -> verifyEquals(
            (config.configData as SmtpAccount),
            jsonObject.get("smtp_account").asJsonObject
        )
        ConfigType.SES_ACCOUNT -> verifyEquals(
            (config.configData as SesAccount),
            jsonObject.get("ses_account").asJsonObject
        )
        ConfigType.EMAIL_GROUP -> verifyEquals(
            (config.configData as EmailGroup),
            jsonObject.get("email_group").asJsonObject
        )
        ConfigType.SNS -> verifyEquals((config.configData as Sns), jsonObject.get("sns").asJsonObject)
        else -> Assert.fail("configType:${config.configType} not handled in test")
    }
}

fun verifySingleConfigEquals(
    configId: String,
    config: NotificationConfig,
    jsonObject: JsonObject,
    totalHits: Int = -1
) {
    if (totalHits >= 0) {
        Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
    }
    val items = jsonObject.get("config_list").asJsonArray
    Assert.assertEquals(1, items.size())
    val getResponseItem = items[0].asJsonObject
    Assert.assertEquals(configId, getResponseItem.get("config_id").asString)
    Assert.assertEquals("", getResponseItem.get("tenant").asString)
    verifyEquals(config, getResponseItem.get("config").asJsonObject)
}

fun verifySingleConfigIdEquals(configId: String, jsonObject: JsonObject, totalHits: Int = -1) {
    if (totalHits >= 0) {
        Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
    }
    val items = jsonObject.get("config_list").asJsonArray
    Assert.assertEquals(1, items.size())
    val getResponseItem = items[0].asJsonObject
    Assert.assertEquals(configId, getResponseItem.get("config_id").asString)
}

fun verifyMultiConfigEquals(
    objectMap: Map<String, NotificationConfig>,
    jsonObject: JsonObject,
    totalHits: Int = -1
) {
    if (totalHits >= 0) {
        Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
    }
    val items = jsonObject.get("config_list").asJsonArray
    Assert.assertEquals(objectMap.size, items.size())
    items.forEach {
        val item = it.asJsonObject
        val configId = item.get("config_id").asString
        Assert.assertNotNull(configId)
        val config = objectMap[configId]
        Assert.assertNotNull(config)
        Assert.assertEquals("", item.get("tenant").asString)
        verifyEquals(config!!, item.get("config").asJsonObject)
    }
}

fun verifyMultiConfigIdEquals(idSet: Set<String>, jsonObject: JsonObject, totalHits: Int = -1) {
    if (totalHits >= 0) {
        Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
    }
    val items = jsonObject.get("config_list").asJsonArray
    Assert.assertEquals(idSet.size, items.size())
    items.forEach {
        val item = it.asJsonObject
        val configId = item.get("config_id").asString
        Assert.assertNotNull(configId)
        Assert.assertTrue(idSet.contains(configId))
    }
}

fun verifyOrderedConfigList(idList: List<String>, jsonObject: JsonObject, totalHits: Int = -1) {
    if (totalHits >= 0) {
        Assert.assertEquals(totalHits, jsonObject.get("total_hits").asInt)
    }
    val items = jsonObject.get("config_list").asJsonArray
    Assert.assertEquals(idList.size, items.size())
    (1..idList.size).forEach {
        Assert.assertEquals(idList[it - 1], items[it - 1].asJsonObject.get("config_id").asString)
    }
}
