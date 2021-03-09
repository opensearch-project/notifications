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

package com.amazon.opendistroforelasticsearch.notifications.util

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.xcontent.DeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestRequest
import java.net.URL
import java.util.EnumSet

val logHelper by logger(NotificationPlugin::class.java)

internal fun StreamInput.createJsonParser(): XContentParser {
    return XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, this)
}

internal fun RestRequest.contentParserNextToken(): XContentParser {
    val parser = this.contentParser()
    parser.nextToken()
    return parser
}

internal fun XContentParser.stringList(): List<String> {
    val retList: MutableList<String> = mutableListOf()
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this::getTokenLocation)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retList.add(text())
    }
    return retList
}

inline fun <reified E : Enum<E>> valueOf(type: String, default: E): E {
    return try {
        java.lang.Enum.valueOf(E::class.java, type)
    } catch (e: IllegalArgumentException) {
        logHelper.info("Enum value $type is not recognized, defaulting to $default")
        default
    }
}

inline fun <reified E : Enum<E>> XContentParser.enumSet(default: E): EnumSet<E> {
    val retSet: EnumSet<E> = EnumSet.noneOf(E::class.java)
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this::getTokenLocation)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retSet.add(valueOf(text(), default))
    }
    return retSet
}

internal fun <T : Any> logger(forClass: Class<T>): Lazy<Logger> {
    return lazy { LogManager.getLogger(forClass) }
}

internal fun XContentBuilder.fieldIfNotNull(name: String, value: Any?): XContentBuilder {
    if (value != null) {
        this.field(name, value)
    }
    return this
}

internal fun XContentBuilder.objectIfNotNull(name: String, xContentObject: ToXContentObject?): XContentBuilder {
    if (xContentObject != null) {
        this.field(name)
        xContentObject.toXContent(this, ToXContent.EMPTY_PARAMS)
    }
    return this
}

@Suppress("UNCHECKED_CAST")
internal fun suppressWarningCast(map: MutableMap<String?, Any?>?): Map<String, String> {
    return map as Map<String, String>
}

internal fun validateUrl(urlString: String) {
    val url = URL(urlString)
    require("https" == url.protocol) // Support only HTTPS. HTTP and other protocols not supported
    // TODO : Add hosts deny list
}
