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
package org.opensearch.notifications.index

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.DESCRIPTION_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_ACCOUNT_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_GROUP_ID_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.FROM_ADDRESS_TAG
import org.opensearch.commons.notifications.NotificationConstants.HOST_TAG
import org.opensearch.commons.notifications.NotificationConstants.IS_ENABLED_TAG
import org.opensearch.commons.notifications.NotificationConstants.METHOD_TAG
import org.opensearch.commons.notifications.NotificationConstants.NAME_TAG
import org.opensearch.commons.notifications.NotificationConstants.QUERY_TAG
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.ROLE_ARN_FIELD
import org.opensearch.commons.notifications.NotificationConstants.TOPIC_ARN_FIELD
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.notifications.model.ConfigType.CHIME
import org.opensearch.commons.notifications.model.ConfigType.EMAIL
import org.opensearch.commons.notifications.model.ConfigType.EMAIL_GROUP
import org.opensearch.commons.notifications.model.ConfigType.SLACK
import org.opensearch.commons.notifications.model.ConfigType.SMTP_ACCOUNT
import org.opensearch.commons.notifications.model.ConfigType.SNS
import org.opensearch.commons.notifications.model.ConfigType.WEBHOOK
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG
import org.opensearch.rest.RestStatus

/**
 * Helper class for Get operations.
 */
object ConfigQueryHelper {
    private const val KEY_PREFIX = CONFIG_TAG

    private val METADATA_RANGE_FIELDS = setOf(
        UPDATED_TIME_TAG,
        CREATED_TIME_TAG
    )
    private val BOOLEAN_FIELDS = setOf(
        IS_ENABLED_TAG
    )
    private val KEYWORD_FIELDS = setOf(
        CONFIG_TYPE_TAG,
        FEATURE_LIST_TAG,
        "${EMAIL.tag}.$EMAIL_ACCOUNT_ID_TAG",
        "${EMAIL.tag}.$EMAIL_GROUP_ID_LIST_TAG",
        "${SMTP_ACCOUNT.tag}.$METHOD_TAG"
    )
    private val TEXT_FIELDS = setOf(
        NAME_TAG,
        DESCRIPTION_TAG,
        "${SLACK.tag}.$URL_TAG",
        "${CHIME.tag}.$URL_TAG",
        "${WEBHOOK.tag}.$URL_TAG",
        "${EMAIL.tag}.$RECIPIENT_LIST_TAG",
        "${SMTP_ACCOUNT.tag}.$HOST_TAG",
        "${SMTP_ACCOUNT.tag}.$FROM_ADDRESS_TAG",
        "${EMAIL_GROUP.tag}.$RECIPIENT_LIST_TAG",
        "${SNS.tag}.$TOPIC_ARN_FIELD",
        "${SNS.tag}.$ROLE_ARN_FIELD"
    )

    private val METADATA_FIELDS = METADATA_RANGE_FIELDS
    private val CONFIG_FIELDS = KEYWORD_FIELDS.union(TEXT_FIELDS)
    private val ALL_FIELDS = METADATA_FIELDS.union(CONFIG_FIELDS).union(BOOLEAN_FIELDS)

    val FILTER_PARAMS = ALL_FIELDS.union(setOf(QUERY_TAG))

    fun getSortField(sortField: String?): String {
        return if (sortField == null) {
            "$METADATA_TAG.$UPDATED_TIME_TAG"
        } else {
            when {
                METADATA_RANGE_FIELDS.contains(sortField) -> "$METADATA_TAG.$sortField"
                BOOLEAN_FIELDS.contains(sortField) -> "$CONFIG_TAG.$sortField"
                KEYWORD_FIELDS.contains(sortField) -> "$CONFIG_TAG.$sortField"
                TEXT_FIELDS.contains(sortField) -> "$CONFIG_TAG.$sortField.keyword"
                else -> throw OpenSearchStatusException("Sort on $sortField not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }
    }

    fun addQueryFilters(query: BoolQueryBuilder, filterParams: Map<String, String>) {
        filterParams.forEach {
            when {
                QUERY_TAG == it.key -> query.filter(getQueryAllBuilder(it.value))
                METADATA_RANGE_FIELDS.contains(it.key) -> query.filter(getRangeQueryBuilder(it.key, it.value))
                BOOLEAN_FIELDS.contains(it.key) -> query.filter(getTermQueryBuilder(it.key, it.value))
                KEYWORD_FIELDS.contains(it.key) -> query.filter(getTermsQueryBuilder(it.key, it.value))
                TEXT_FIELDS.contains(it.key) -> query.filter(getMatchQueryBuilder(it.key, it.value))
                else -> throw OpenSearchStatusException("Query on ${it.key} not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }
    }

    private fun getQueryAllBuilder(queryValue: String): QueryBuilder {
        val allQuery = QueryBuilders.queryStringQuery(queryValue)
        // Searching on metadata field is not supported. skip adding METADATA_FIELDS
        CONFIG_FIELDS.forEach {
            allQuery.field("$KEY_PREFIX.$it")
        }
        return allQuery
    }

    private fun getRangeQueryBuilder(queryKey: String, queryValue: String): QueryBuilder {
        val range = queryValue.split("..")
        return when (range.size) {
            1 -> QueryBuilders.termQuery("$METADATA_TAG.$queryKey", queryValue)
            2 -> {
                val rangeQuery = QueryBuilders.rangeQuery("$METADATA_TAG.$queryKey")
                rangeQuery.from(range[0])
                rangeQuery.to(range[1])
                rangeQuery
            }
            else -> {
                throw OpenSearchStatusException(
                    "Invalid Range format $queryValue, allowed format 'exact' or 'from..to'",
                    RestStatus.NOT_ACCEPTABLE
                )
            }
        }
    }

    private fun getTermQueryBuilder(queryKey: String, queryValue: String): QueryBuilder {
        return QueryBuilders.termQuery("$KEY_PREFIX.$queryKey", queryValue)
    }

    private fun getTermsQueryBuilder(queryKey: String, queryValue: String): QueryBuilder {
        return QueryBuilders.termsQuery("$KEY_PREFIX.$queryKey", queryValue.split(","))
    }

    private fun getMatchQueryBuilder(queryKey: String, queryValue: String): QueryBuilder {
        return QueryBuilders.matchQuery("$KEY_PREFIX.$queryKey", queryValue)
    }
}
