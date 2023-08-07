/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.index

import org.apache.lucene.search.join.ScoreMode
import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TAG
import org.opensearch.commons.notifications.NotificationConstants.CONFIG_TYPE_TAG
import org.opensearch.commons.notifications.NotificationConstants.CREATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.DESCRIPTION_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_ACCOUNT_ID_TAG
import org.opensearch.commons.notifications.NotificationConstants.EMAIL_GROUP_ID_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.FROM_ADDRESS_TAG
import org.opensearch.commons.notifications.NotificationConstants.HOST_TAG
import org.opensearch.commons.notifications.NotificationConstants.IS_ENABLED_TAG
import org.opensearch.commons.notifications.NotificationConstants.METHOD_TAG
import org.opensearch.commons.notifications.NotificationConstants.NAME_TAG
import org.opensearch.commons.notifications.NotificationConstants.QUERY_TAG
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_TAG
import org.opensearch.commons.notifications.NotificationConstants.REGION_TAG
import org.opensearch.commons.notifications.NotificationConstants.ROLE_ARN_TAG
import org.opensearch.commons.notifications.NotificationConstants.TOPIC_ARN_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.notifications.model.ConfigType.CHIME
import org.opensearch.commons.notifications.model.ConfigType.EMAIL
import org.opensearch.commons.notifications.model.ConfigType.EMAIL_GROUP
import org.opensearch.commons.notifications.model.ConfigType.MICROSOFT_TEAMS
import org.opensearch.commons.notifications.model.ConfigType.SES_ACCOUNT
import org.opensearch.commons.notifications.model.ConfigType.SLACK
import org.opensearch.commons.notifications.model.ConfigType.SMTP_ACCOUNT
import org.opensearch.commons.notifications.model.ConfigType.SNS
import org.opensearch.commons.notifications.model.ConfigType.WEBHOOK
import org.opensearch.core.rest.RestStatus
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.NotificationPlugin.Companion.TEXT_QUERY_TAG
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG

/**
 * Helper class for Get operations.
 */
object ConfigQueryHelper {
    private const val KEY_PREFIX = CONFIG_TAG
    private const val KEYWORD_SUFFIX = "keyword"

    private val NESTED_PATHS = listOf(
        "${EMAIL.tag}.$RECIPIENT_LIST_TAG",
        "${EMAIL_GROUP.tag}.$RECIPIENT_LIST_TAG"
    )
    private val METADATA_RANGE_FIELDS = setOf(
        UPDATED_TIME_TAG,
        CREATED_TIME_TAG
    )
    private val BOOLEAN_FIELDS = setOf(
        IS_ENABLED_TAG
    )
    private val KEYWORD_FIELDS = setOf(
        CONFIG_TYPE_TAG,
        "${EMAIL.tag}.$EMAIL_ACCOUNT_ID_TAG",
        "${EMAIL.tag}.$EMAIL_GROUP_ID_LIST_TAG",
        "${SMTP_ACCOUNT.tag}.$METHOD_TAG",
        "${SES_ACCOUNT.tag}.$REGION_TAG",
        // Text fields with keyword
        "$NAME_TAG.$KEYWORD_SUFFIX",
        "$DESCRIPTION_TAG.$KEYWORD_SUFFIX",
        "${SLACK.tag}.$URL_TAG.$KEYWORD_SUFFIX",
        "${CHIME.tag}.$URL_TAG.$KEYWORD_SUFFIX",
        "${MICROSOFT_TEAMS.tag}.$URL_TAG.$KEYWORD_SUFFIX",
        "${WEBHOOK.tag}.$URL_TAG.$KEYWORD_SUFFIX",
        "${SMTP_ACCOUNT.tag}.$HOST_TAG.$KEYWORD_SUFFIX",
        "${SMTP_ACCOUNT.tag}.$FROM_ADDRESS_TAG.$KEYWORD_SUFFIX",
        "${SNS.tag}.$TOPIC_ARN_TAG.$KEYWORD_SUFFIX",
        "${SNS.tag}.$ROLE_ARN_TAG.$KEYWORD_SUFFIX",
        "${SES_ACCOUNT.tag}.$ROLE_ARN_TAG.$KEYWORD_SUFFIX",
        "${SES_ACCOUNT.tag}.$FROM_ADDRESS_TAG.$KEYWORD_SUFFIX"
    )
    private val TEXT_FIELDS = setOf(
        NAME_TAG,
        DESCRIPTION_TAG,
        "${SLACK.tag}.$URL_TAG",
        "${CHIME.tag}.$URL_TAG",
        "${MICROSOFT_TEAMS.tag}.$URL_TAG",
        "${WEBHOOK.tag}.$URL_TAG",
        "${SMTP_ACCOUNT.tag}.$HOST_TAG",
        "${SMTP_ACCOUNT.tag}.$FROM_ADDRESS_TAG",
        "${SNS.tag}.$TOPIC_ARN_TAG",
        "${SNS.tag}.$ROLE_ARN_TAG",
        "${SES_ACCOUNT.tag}.$ROLE_ARN_TAG",
        "${SES_ACCOUNT.tag}.$FROM_ADDRESS_TAG"
    )
    private val NESTED_KEYWORD_FIELDS = setOf(
        // Text fields with keyword
        "${EMAIL.tag}.$RECIPIENT_LIST_TAG.$RECIPIENT_TAG.$KEYWORD_SUFFIX",
        "${EMAIL_GROUP.tag}.$RECIPIENT_LIST_TAG.$RECIPIENT_TAG.$KEYWORD_SUFFIX"
    )
    private val NESTED_TEXT_FIELDS = setOf(
        "${EMAIL.tag}.$RECIPIENT_LIST_TAG.$RECIPIENT_TAG",
        "${EMAIL_GROUP.tag}.$RECIPIENT_LIST_TAG.$RECIPIENT_TAG"
    )

    private val METADATA_FIELDS = METADATA_RANGE_FIELDS
    private val CONFIG_FIELDS = KEYWORD_FIELDS.union(TEXT_FIELDS)
    private val NESTED_FIELDS = NESTED_KEYWORD_FIELDS.union(NESTED_TEXT_FIELDS)
    private val ALL_FIELDS = METADATA_FIELDS.union(CONFIG_FIELDS).union(BOOLEAN_FIELDS).union(NESTED_FIELDS)

    val FILTER_PARAMS = ALL_FIELDS.union(setOf(QUERY_TAG, TEXT_QUERY_TAG))

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
                TEXT_QUERY_TAG == it.key -> query.filter(getTextQueryAllBuilder(it.value))
                METADATA_RANGE_FIELDS.contains(it.key) -> query.filter(getRangeQueryBuilder(it.key, it.value))
                BOOLEAN_FIELDS.contains(it.key) -> query.filter(getTermQueryBuilder(it.key, it.value))
                KEYWORD_FIELDS.contains(it.key) -> query.filter(getTermsQueryBuilder(it.key, it.value))
                TEXT_FIELDS.contains(it.key) -> query.filter(getMatchQueryBuilder(it.key, it.value))
                NESTED_TEXT_FIELDS.contains(it.key) -> query.filter(getNestedMatchQueryBuilder(it.key, it.value))
                else -> throw OpenSearchStatusException("Query on ${it.key} not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }
    }

    private fun getQueryAllBuilder(queryValue: String): QueryBuilder {
        val boolQuery = QueryBuilders.boolQuery()
        val allQuery = QueryBuilders.queryStringQuery(queryValue)
        // Searching on metadata field is not supported. skip adding METADATA_FIELDS
        CONFIG_FIELDS.forEach {
            allQuery.field("$KEY_PREFIX.$it")
        }
        boolQuery.should(allQuery)
        NESTED_PATHS.forEach { path ->
            run {
                val allNestedQuery = QueryBuilders.queryStringQuery(queryValue)
                val fields = NESTED_FIELDS.filter { it.startsWith(path) }
                fields.forEach {
                    allNestedQuery.field("$KEY_PREFIX.$it")
                }
                val nestedFieldQuery = QueryBuilders.nestedQuery("$KEY_PREFIX.$path", allNestedQuery, ScoreMode.None)
                boolQuery.should(nestedFieldQuery)
            }
        }
        return boolQuery
    }

    private fun getTextQueryAllBuilder(queryValue: String): QueryBuilder {
        val boolQuery = QueryBuilders.boolQuery()
        val allQuery = QueryBuilders.queryStringQuery(queryValue)
        // Searching on metadata field is not supported. skip adding METADATA_FIELDS
        TEXT_FIELDS.forEach {
            allQuery.field("$KEY_PREFIX.$it")
        }
        boolQuery.should(allQuery)
        NESTED_PATHS.forEach { path ->
            run {
                val allNestedQuery = QueryBuilders.queryStringQuery(queryValue)
                val fields = NESTED_TEXT_FIELDS.filter { it.startsWith(path) }
                fields.forEach {
                    allNestedQuery.field("$KEY_PREFIX.$it")
                }
                val nestedFieldQuery = QueryBuilders.nestedQuery("$KEY_PREFIX.$path", allNestedQuery, ScoreMode.None)
                boolQuery.should(nestedFieldQuery)
            }
        }
        return boolQuery
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

    private fun getNestedMatchQueryBuilder(queryKey: String, queryValue: String): QueryBuilder {
        val query = QueryBuilders.matchQuery("$KEY_PREFIX.$queryKey", queryValue)
        val nestedPath = NESTED_PATHS.first { queryKey.startsWith(it) }
        return QueryBuilders.nestedQuery("$KEY_PREFIX.$nestedPath", query, ScoreMode.None)
    }
}
