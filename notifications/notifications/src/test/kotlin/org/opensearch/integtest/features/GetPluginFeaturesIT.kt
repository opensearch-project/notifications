/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest.features

import org.junit.Assert
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.integtest.PluginRestTestCase
import org.opensearch.notifications.NotificationPlugin.Companion.PLUGIN_BASE_URI
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class GetPluginFeaturesIT : PluginRestTestCase() {

    fun `test Get plugin features should return non-empty configTypes`() {
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/features",
            "",
            RestStatus.OK.status
        )
        Assert.assertFalse(getResponse.get("allowed_config_type_list").asJsonArray.isEmpty)
        Assert.assertFalse(getResponse.get("allowed_config_feature_list").asJsonArray.isEmpty)
        val pluginFeatures = getResponse.get("plugin_features").asJsonObject
        Assert.assertFalse(pluginFeatures.keySet().isEmpty())
    }

    fun `test If configTypes contains email then it should also contain email_group and smtp_account OR ses`() {
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$PLUGIN_BASE_URI/features",
            "",
            RestStatus.OK.status
        )
        val configTypes = getResponse.get("allowed_config_type_list").asJsonArray.map { it.asString }
        if (configTypes.contains(ConfigType.EMAIL.tag)) {
            Assert.assertTrue(configTypes.contains(ConfigType.EMAIL_GROUP.tag))
            Assert.assertTrue(configTypes.contains(ConfigType.SMTP_ACCOUNT.tag) || configTypes.contains(ConfigType.SES_ACCOUNT.tag))
        }
    }
}
