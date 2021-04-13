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

import org.apache.lucene.search.TotalHits
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.xcontent.XContentParser

/**
 * FeatureChannel search results
 */
class FeatureChannelList : SearchResults<FeatureChannel> {
    companion object {
        private const val FEATURE_CONFIG_LIST_TAG = "featureChannelList"
    }

    /**
     * single item result constructor
     */
    constructor(objectItem: FeatureChannel) : super(FEATURE_CONFIG_LIST_TAG, objectItem)

    /**
     * all param constructor
     */
    constructor(
        startIndex: Long,
        totalHits: Long,
        totalHitRelation: TotalHits.Relation,
        objectList: List<FeatureChannel>
    ) : super(startIndex, totalHits, totalHitRelation, FEATURE_CONFIG_LIST_TAG, objectList)

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : super(input, FeatureChannel.reader)

    /**
     * Construct object from XContentParser
     */
    constructor(parser: XContentParser) : super(parser, FEATURE_CONFIG_LIST_TAG)

    /**
     * Construct object from SearchResponse
     */
    constructor(from: Long, response: SearchResponse, searchHitParser: SearchHitParser) : super(
        from,
        response,
        searchHitParser,
        FEATURE_CONFIG_LIST_TAG
    )

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser): FeatureChannel {
        return FeatureChannel.parse(parser)
    }
}
