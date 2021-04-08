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
package com.amazon.opendistroforelasticsearch.commons.notifications.model

import com.amazon.opendistroforelasticsearch.commons.utils.createObjectFromJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.getJsonString
import com.amazon.opendistroforelasticsearch.commons.utils.recreateObject
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.MalformedURLException

internal class SlackTests {

    @Test
    fun `Slack serialize and deserialize transport object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val recreatedObject = recreateObject(sampleSlack) { Slack(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack serialize and deserialize using json object should be equal`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = getJsonString(sampleSlack)
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack should deserialize json object using parser`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleSlack.url}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }

    @Test
    fun `Slack should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url is replace with url2 in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url2\":\"${sampleSlack.url}\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url is not proper`() {
        assertThrows<MalformedURLException> {
            Slack("domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"domain.com/sample_url\"}"
        assertThrows<MalformedURLException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should throw exception when url protocol is not https`() {
        assertThrows<IllegalArgumentException> {
            Slack("http://domain.com/sample_url#1234567890")
        }
        val jsonString = "{\"url\":\"http://domain.com/sample_url\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { Slack.parse(it) }
        }
    }

    @Test
    fun `Slack should safely ignore extra field in json object`() {
        val sampleSlack = Slack("https://domain.com/sample_url#1234567890")
        val jsonString = "{\"url\":\"${sampleSlack.url}\", \"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Slack.parse(it) }
        assertEquals(sampleSlack, recreatedObject)
    }
}
