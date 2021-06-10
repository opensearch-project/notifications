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

package org.opensearch.notifications.spi.message

import org.apache.http.client.utils.URIBuilder
import org.opensearch.common.Strings
import org.opensearch.notifications.spi.model.DestinationType
import org.opensearch.notifications.spi.model.MessageContent
import java.net.URI
import java.net.URISyntaxException

open class WebhookMessage(
    val url: String,
    title: String,
    configType: DestinationType,
    messageContent: MessageContent,
    channelId: String,
) : BaseMessage(title, configType, messageContent, channelId) {

    init {
        require(!Strings.isNullOrEmpty(url)) { "url is invalid or empty" }
    }

    @SuppressWarnings("SwallowedException")
    internal fun buildUri(): URI {
        return try {
            URIBuilder(url).build()
        } catch (exception: URISyntaxException) {
            throw IllegalStateException("Error creating URI")
        }
    }

    // TODO build complete message with title?
    override fun toString(): String {
        return "DestinationType: $configType , Url: $url , Message: ${channelMessage.textDescription} , Title: $title"
    }
}
