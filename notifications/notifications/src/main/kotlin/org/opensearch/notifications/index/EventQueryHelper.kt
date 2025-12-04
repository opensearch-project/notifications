/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.index

import org.apache.lucene.search.join.ScoreMode
import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_NAME_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.DELIVERY_STATUS_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_RECIPIENT_STATUS_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_SOURCE_TAG
import org.opensearch.commons.notifications.NotificationConstants.EVENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.QUERY_TAG
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.REFERENCE_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.SEVERITY_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_CODE_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_TEXT_TAG
import org.opensearch.commons.notifications.NotificationConstants.TAGS_TAG
import org.opensearch.commons.notifications.NotificationConstants.TITLE_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.core.rest.RestStatus
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.NotificationPlugin.Companion.TEXT_QUERY_TAG
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG

/**
 * Helper class for Get operations.
 */
object EventQueryHelper {
    private const val KEY_PREFIX = EVENT_TAG
    private const val NESTED_PATH = "$KEY_PREFIX.$STATUS_LIST_TAG"
    private const val KEYWORD_SUFFIX = "keyword"

    private val METADATA_RANGE_FIELDS =
        setOf(
            UPDATED_TIME_TAG,
            CREATED_TIME_TAG,
        )
    private val KEYWORD_FIELDS =
        setOf(
            "$EVENT_SOURCE_TAG.$REFERENCE_ID_TAG",
            "$EVENT_SOURCE_TAG.$SEVERITY_TAG",
            // Text fields with keyword
            "$EVENT_SOURCE_TAG.$TAGS_TAG.$KEYWORD_SUFFIX",
            "$EVENT_SOURCE_TAG.$TITLE_TAG.$KEYWORD_SUFFIX",
        )
    private val TEXT_FIELDS =
        setOf(
            "$EVENT_SOURCE_TAG.$TAGS_TAG",
            "$EVENT_SOURCE_TAG.$TITLE_TAG",
        )
    private val NESTED_KEYWORD_FIELDS =
        setOf(
            "$STATUS_LIST_TAG.$CONFIG_ID_TAG",
            "$STATUS_LIST_TAG.$CONFIG_TYPE_TAG",
            "$STATUS_LIST_TAG.$EMAIL_RECIPIENT_STATUS_TAG.$DELIVERY_STATUS_TAG.$STATUS_CODE_TAG",
            "$STATUS_LIST_TAG.$DELIVERY_STATUS_TAG.$STATUS_CODE_TAG",
            // Text fields with keyword
            "$STATUS_LIST_TAG.$CONFIG_NAME_TAG.$KEYWORD_SUFFIX",
            "$STATUS_LIST_TAG.$EMAIL_RECIPIENT_STATUS_TAG.$RECIPIENT_TAG.$KEYWORD_SUFFIX",
            "$STATUS_LIST_TAG.$EMAIL_RECIPIENT_STATUS_TAG.$DELIVERY_STATUS_TAG.$STATUS_TEXT_TAG.$KEYWORD_SUFFIX",
            "$STATUS_LIST_TAG.$DELIVERY_STATUS_TAG.$STATUS_TEXT_TAG.$KEYWORD_SUFFIX",
        )
    private val NESTED_TEXT_FIELDS =
        setOf(
            "$STATUS_LIST_TAG.$CONFIG_NAME_TAG",
            "$STATUS_LIST_TAG.$EMAIL_RECIPIENT_STATUS_TAG.$RECIPIENT_TAG",
            "$STATUS_LIST_TAG.$EMAIL_RECIPIENT_STATUS_TAG.$DELIVERY_STATUS_TAG.$STATUS_TEXT_TAG",
            "$STATUS_LIST_TAG.$DELIVERY_STATUS_TAG.$STATUS_TEXT_TAG",
        )

    private val METADATA_FIELDS = METADATA_RANGE_FIELDS
    private val EVENT_FIELDS = KEYWORD_FIELDS.union(TEXT_FIELDS)
    private val NESTED_FIELDS = NESTED_KEYWORD_FIELDS.union(NESTED_TEXT_FIELDS)
    private val ALL_FIELDS = METADATA_FIELDS.union(EVENT_FIELDS).union(NESTED_FIELDS)

    val FILTER_PARAMS = ALL_FIELDS.union(setOf(QUERY_TAG, TEXT_QUERY_TAG))

    fun getSortField(sortField: String?): String =
        if (sortField == null) {
            "$METADATA_TAG.$UPDATED_TIME_TAG"
        } else {
            when {
                METADATA_RANGE_FIELDS.contains(sortField) -> "$METADATA_TAG.$sortField"
                KEYWORD_FIELDS.contains(sortField) -> "$KEY_PREFIX.$sortField"
                TEXT_FIELDS.contains(sortField) -> "$KEY_PREFIX.$sortField.keyword"
                else -> throw OpenSearchStatusException("Sort on $sortField not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }

    fun addQueryFilters(
        query: BoolQueryBuilder,
        filterParams: Map<String, String>,
    ) {
        val nestedQuery = QueryBuilders.boolQuery()
        filterParams.forEach {
            when {
                QUERY_TAG == it.key -> query.filter(getQueryAllBuilder(it.value))
                TEXT_QUERY_TAG == it.key -> query.filter(getTextQueryAllBuilder(it.value))
                METADATA_RANGE_FIELDS.contains(it.key) -> query.filter(getRangeQueryBuilder(it.key, it.value))
                KEYWORD_FIELDS.contains(it.key) -> query.filter(getTermsQueryBuilder(it.key, it.value))
                TEXT_FIELDS.contains(it.key) -> query.filter(getMatchQueryBuilder(it.key, it.value))
                NESTED_KEYWORD_FIELDS.contains(it.key) -> nestedQuery.filter(getTermsQueryBuilder(it.key, it.value))
                NESTED_TEXT_FIELDS.contains(it.key) -> nestedQuery.filter(getMatchQueryBuilder(it.key, it.value))
                else -> throw OpenSearchStatusException("Query on ${it.key} not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }
        if (nestedQuery.filter().isNotEmpty()) {
            query.filter(QueryBuilders.nestedQuery(NESTED_PATH, nestedQuery, ScoreMode.None))
        }
    }

    private fun getQueryAllBuilder(queryValue: String): QueryBuilder {
        val boolQuery = QueryBuilders.boolQuery()
        val allQuery = QueryBuilders.queryStringQuery(queryValue)
        val allNestedQuery = QueryBuilders.queryStringQuery(queryValue)
        // Searching on metadata field is not supported. skip adding METADATA_FIELDS
        EVENT_FIELDS.forEach {
            allQuery.field("$KEY_PREFIX.$it")
        }
        NESTED_FIELDS.forEach {
            allNestedQuery.field("$KEY_PREFIX.$it")
        }
        val nestedFieldQuery = QueryBuilders.nestedQuery(NESTED_PATH, allNestedQuery, ScoreMode.None)
        boolQuery.should(allQuery)
        boolQuery.should(nestedFieldQuery)
        return boolQuery
    }

    private fun getTextQueryAllBuilder(queryValue: String): QueryBuilder {
        val boolQuery = QueryBuilders.boolQuery()
        val allQuery = QueryBuilders.queryStringQuery(queryValue)
        val allNestedQuery = QueryBuilders.queryStringQuery(queryValue)
        // Searching on metadata field is not supported. skip adding METADATA_FIELDS
        TEXT_FIELDS.forEach {
            allQuery.field("$KEY_PREFIX.$it")
        }
        NESTED_TEXT_FIELDS.forEach {
            allNestedQuery.field("$KEY_PREFIX.$it")
        }
        val nestedFieldQuery = QueryBuilders.nestedQuery(NESTED_PATH, allNestedQuery, ScoreMode.None)
        boolQuery.should(allQuery)
        boolQuery.should(nestedFieldQuery)
        return boolQuery
    }

    private fun getRangeQueryBuilder(
        queryKey: String,
        queryValue: String,
    ): QueryBuilder {
        val range = queryValue.split("..")
        return when (range.size) {
            1 -> {
                QueryBuilders.termQuery("$METADATA_TAG.$queryKey", queryValue)
            }

            2 -> {
                val rangeQuery = QueryBuilders.rangeQuery("$METADATA_TAG.$queryKey")
                rangeQuery.from(range[0])
                rangeQuery.to(range[1])
                rangeQuery
            }

            else -> {
                throw OpenSearchStatusException(
                    "Invalid Range format $queryValue, allowed format 'exact' or 'from..to'",
                    RestStatus.NOT_ACCEPTABLE,
                )
            }
        }
    }

    private fun getTermsQueryBuilder(
        queryKey: String,
        queryValue: String,
    ): QueryBuilder = QueryBuilders.termsQuery("$KEY_PREFIX.$queryKey", queryValue.split(","))

    private fun getMatchQueryBuilder(
        queryKey: String,
        queryValue: String,
    ): QueryBuilder = QueryBuilders.matchQuery("$KEY_PREFIX.$queryKey", queryValue)
}
