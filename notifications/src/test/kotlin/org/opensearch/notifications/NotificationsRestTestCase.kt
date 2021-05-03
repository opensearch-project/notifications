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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import org.junit.After
import org.junit.Before
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.notifications.resthandler.SendMessageRestHandler.Companion.SEND_BASE_URI
import org.opensearch.notifications.settings.PluginSettings
import org.springframework.integration.test.mail.TestMailServer

abstract class NotificationsRestTestCase : PluginRestTestCase() {

    private val smtpPort = PluginSettings.smtpPort
    private val smtpServer: TestMailServer.SmtpServer
    private val fromAddress = "from@email.com"

    init {
        smtpServer = TestMailServer.smtp(smtpPort)
    }

    @Before
    @Throws(InterruptedException::class)
    fun setupNotification() {
        resetFromAddress()
        init()
    }

    @After
    open fun tearDownServer() {
        smtpServer.stop()
        smtpServer.resetServer()
    }

    protected fun executeRequest(
        refTag: String,
        recipients: List<String>,
        title: String,
        textDescription: String,
        htmlDescription: String,
        attachment: JsonObject
    ): JsonObject {
        val request = buildRequest(refTag, recipients, title, textDescription, htmlDescription, attachment)
        return executeRequest(request)
    }

    protected fun buildRequest(
        refTag: String,
        recipients: List<String>,
        title: String,
        textDescription: String,
        htmlDescription: String,
        attachment: JsonObject
    ): Request {
        val request = Request("POST", SEND_BASE_URI)

        val jsonEntity = NotificationsJsonEntity.Builder()
            .setRefTag(refTag)
            .setRecipients(recipients)
            .setTitle(title)
            .setTextDescription(textDescription)
            .setHtmlDescription(htmlDescription)
            .setAttachment(attachment.toString())
            .build()
        request.setJsonEntity(jsonEntity.getJsonEntityAsString())

        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return request
    }

    /** Provided for each test to load test index, data and other setup work */
    protected open fun init() {}

    protected fun setFromAddress(address: String): JsonObject? {
        return updateClusterSettings(
            ClusterSetting(
                "persistent", "opensearch.notifications.email.fromAddress", address
            )
        )
    }

    protected fun resetFromAddress(): JsonObject? {
        return setFromAddress(fromAddress)
    }

    protected fun setChannelType(type: String) {
        updateClusterSettings(
            ClusterSetting(
                "persistent", "opensearch.notifications.email.channel", type
            )
        )
    }

    protected fun resetChannelType() {
        setChannelType(PluginSettings.emailChannel)
    }
}
