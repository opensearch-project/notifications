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
import org.opensearch.commons.notifications.NotificationConstants.RECIPIENT_LIST_TAG
import org.opensearch.commons.notifications.NotificationConstants.UPDATED_TIME_TAG
import org.opensearch.commons.notifications.NotificationConstants.URL_TAG
import org.opensearch.commons.notifications.model.ConfigType.CHIME
import org.opensearch.commons.notifications.model.ConfigType.EMAIL
import org.opensearch.commons.notifications.model.ConfigType.EMAIL_GROUP
import org.opensearch.commons.notifications.model.ConfigType.SLACK
import org.opensearch.commons.notifications.model.ConfigType.SMTP_ACCOUNT
import org.opensearch.commons.notifications.model.ConfigType.WEBHOOK
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.model.NotificationConfigDoc.Companion.CONFIG_TAG
import org.opensearch.notifications.model.NotificationConfigDoc.Companion.METADATA_TAG
import org.opensearch.rest.RestStatus

/**
 * Helper class for Get operations.
 */
object QueryHelper {
    private val METADATA_FIELDS = setOf(
        UPDATED_TIME_TAG,
        CREATED_TIME_TAG
    )
    private val KEYWORD_FIELDS = setOf(
        CONFIG_TYPE_TAG,
        IS_ENABLED_TAG,
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
        "${EMAIL_GROUP.tag}.$RECIPIENT_LIST_TAG"
    )

    fun getSortField(sortField: String?): String {
        return if (sortField == null) {
            "$METADATA_TAG.$UPDATED_TIME_TAG"
        } else {
            when {
                METADATA_FIELDS.contains(sortField) -> "$METADATA_TAG.$sortField"
                KEYWORD_FIELDS.contains(sortField) -> "$CONFIG_TAG.$sortField"
                TEXT_FIELDS.contains(sortField) -> "$CONFIG_TAG.$sortField.keyword"
                else -> throw OpenSearchStatusException("Sort on $sortField not acceptable", RestStatus.NOT_ACCEPTABLE)
            }
        }
    }

    fun getQueryBuilder(prefix: String, queryKey: String, queryValue: String): QueryBuilder {
        return when {
            KEYWORD_FIELDS.contains(queryKey) -> getTermsQueryBuilder(prefix, queryKey, queryValue)
            TEXT_FIELDS.contains(queryKey) -> getMatchQueryBuilder(prefix, queryKey, queryValue)
            else -> throw OpenSearchStatusException("Query on $queryKey not acceptable", RestStatus.NOT_ACCEPTABLE)
        }
    }

    private fun getTermsQueryBuilder(prefix: String, queryKey: String, queryValue: String): QueryBuilder {
        return QueryBuilders.termsQuery("$prefix.$queryKey", queryValue.split(","))
    }

    private fun getMatchQueryBuilder(prefix: String, queryKey: String, queryValue: String): QueryBuilder {
        return QueryBuilders.matchQuery("$prefix.$queryKey", queryValue)
    }
}
