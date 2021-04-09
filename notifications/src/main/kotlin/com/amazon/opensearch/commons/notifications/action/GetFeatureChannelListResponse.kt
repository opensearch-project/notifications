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
package com.amazon.opensearch.commons.notifications.action

import com.amazon.opensearch.commons.notifications.model.FeatureChannelList
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
class GetFeatureChannelListResponse : com.amazon.opensearch.commons.notifications.action.BaseResponse {
    val searchResult: FeatureChannelList

    companion object {

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetFeatureChannelListResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetFeatureChannelListResponse {
            return GetFeatureChannelListResponse(FeatureChannelList(parser))
        }
    }

    /**
     * constructor for creating the class
     * @param searchResult the notification configuration list
     */
    constructor(searchResult: FeatureChannelList) {
        this.searchResult = searchResult
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        searchResult = FeatureChannelList(input)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        searchResult.writeTo(output)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return searchResult.toXContent(builder, params)
    }
}
