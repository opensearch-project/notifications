/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentType
import org.opensearch.core.xcontent.DeprecationHandler
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentParser
import java.io.ByteArrayOutputStream

fun getJsonString(xContent: ToXContent): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
        xContent.toXContent(builder, ToXContent.EMPTY_PARAMS)
        builder.close()
        return byteArrayOutputStream.toString("UTF8")
    }
}

inline fun <reified CreateType> createObjectFromJsonString(
    jsonString: String,
    block: (XContentParser) -> CreateType,
): CreateType {
    val parser =
        XContentType.JSON
            .xContent()
            .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, jsonString)
    parser.nextToken()
    return block(parser)
}
