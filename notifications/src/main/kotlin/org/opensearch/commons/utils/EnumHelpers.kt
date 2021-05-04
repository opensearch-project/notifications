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

package org.opensearch.commons.utils

import org.apache.logging.log4j.Logger
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import java.util.EnumSet

inline fun <reified E : Enum<E>> valueOf(type: String, default: E, logHelper: Logger): E {
    return try {
        java.lang.Enum.valueOf(E::class.java, type)
    } catch (e: IllegalArgumentException) {
        logHelper.info("${e.message}:Enum value $type is not recognized, defaulting to $default")
        default
    }
}

inline fun <reified E : Enum<E>> XContentParser.enumSet(default: E, logHelper: Logger): EnumSet<E> {
    val retSet: EnumSet<E> = EnumSet.noneOf(E::class.java)
    XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, currentToken(), this)
    while (nextToken() != XContentParser.Token.END_ARRAY) {
        retSet.add(valueOf(text(), default, logHelper))
    }
    return retSet
}

inline fun <reified E : Enum<E>> enumReader(enumClass: Class<E>): Writeable.Reader<E> {
    return Writeable.Reader<E> {
        it.readEnum(enumClass)
    }
}

inline fun <reified E : Enum<E>> enumWriter(ignore: Class<E>): Writeable.Writer<E> {
    return Writeable.Writer<E> { streamOutput: StreamOutput, value: E ->
        streamOutput.writeEnum(value)
    }
}
