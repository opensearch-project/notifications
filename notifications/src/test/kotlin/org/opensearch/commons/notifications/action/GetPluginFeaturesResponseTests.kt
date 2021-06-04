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
package org.opensearch.commons.notifications.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.createObjectFromJsonString
import org.opensearch.commons.utils.getJsonString
import org.opensearch.commons.utils.recreateObject

internal class GetPluginFeaturesResponseTests {

    private fun assertResponseEquals(
        expected: GetPluginFeaturesResponse,
        actual: GetPluginFeaturesResponse
    ) {
        assertEquals(expected.configTypeList, actual.configTypeList)
        assertEquals(expected.pluginFeatures, actual.pluginFeatures)
    }

    @Test
    fun `Get Response serialize and deserialize transport object should be equal`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val recreatedObject = recreateObject(response) { GetPluginFeaturesResponse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response serialize and deserialize using json config object should be equal`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response should safely ignore extra field in json object`() {
        val response = GetPluginFeaturesResponse(
            listOf("config_type_1", "config_type_2", "config_type_3"),
            mapOf(
                Pair("FeatureKey1", "FeatureValue1"),
                Pair("FeatureKey2", "FeatureValue2"),
                Pair("FeatureKey3", "FeatureValue3")
            )
        )
        val jsonString = """
        {
            "config_type_list":["config_type_1", "config_type_2", "config_type_3"],
            "plugin_features":{
                "FeatureKey1":"FeatureValue1",
                "FeatureKey2":"FeatureValue2",
                "FeatureKey3":"FeatureValue3"
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        assertResponseEquals(response, recreatedObject)
    }

    @Test
    fun `Get Response should throw exception if config_type_list is absent in json`() {
        val jsonString = """
        {
            "plugin_features":{
                "FeatureKey1":"FeatureValue1",
                "FeatureKey2":"FeatureValue2",
                "FeatureKey3":"FeatureValue3"
            }
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        }
    }

    @Test
    fun `Get Response should throw exception if plugin_features is absent in json`() {
        val jsonString = """
        {
            "config_type_list":["config_type_1", "config_type_2", "config_type_3"]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetPluginFeaturesResponse.parse(it) }
        }
    }
}
