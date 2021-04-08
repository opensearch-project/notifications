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

package com.amazon.opendistroforelasticsearch.commons.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.elasticsearch.common.io.stream.InputStreamStreamInput
import org.elasticsearch.common.io.stream.OutputStreamStreamOutput
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.Writeable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Suppress("TooManyFunctions")

internal fun <T : Any> logger(forClass: Class<T>): Lazy<Logger> {
    return lazy { LogManager.getLogger(forClass) }
}

@Suppress("UNCHECKED_CAST")
internal fun suppressWarningCast(map: MutableMap<String?, Any?>?): Map<String, String> {
    return map as Map<String, String>
}

/**
 * Re create the object from the writeable.
 * This method needs to be inline and reified so that when this is called from
 * doExecute() of transport action, the object may be created from other JVM.
 */
inline fun <reified Request> recreateObject(writeable: Writeable, block: (StreamInput) -> Request): Request {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        OutputStreamStreamOutput(byteArrayOutputStream).use {
            writeable.writeTo(it)
            InputStreamStreamInput(ByteArrayInputStream(byteArrayOutputStream.toByteArray())).use { streamInput ->
                return block(streamInput)
            }
        }
    }
}
