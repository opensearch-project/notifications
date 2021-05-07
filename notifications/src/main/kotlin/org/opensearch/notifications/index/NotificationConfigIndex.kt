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

import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.get.MultiGetRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.update.UpdateRequest
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.notifications.NotificationConstants.TENANT_TAG
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.SearchResults
import org.opensearch.commons.utils.logger
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.QueryHelper.getSortField
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata.Companion.ACCESS_LIST_TAG
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.model.NotificationConfigDoc.Companion.CONFIG_TAG
import org.opensearch.notifications.model.NotificationConfigDoc.Companion.METADATA_TAG
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.util.SecureIndexClient
import org.opensearch.rest.RestStatus
import org.opensearch.search.SearchHit
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.SortOrder
import java.util.concurrent.TimeUnit

/**
 * Class for doing index operations to maintain configurations in cluster.
 */
@Suppress("TooManyFunctions")
internal object NotificationConfigIndex {
    private val log by logger(NotificationConfigIndex::class.java)
    private const val INDEX_NAME = ".opensearch-notifications-config"
    private const val MAPPING_FILE_NAME = "notifications-config-mapping.yml"
    private const val SETTINGS_FILE_NAME = "notifications-config-settings.yml"
    private const val MAPPING_TYPE = "_doc"

    private lateinit var client: Client
    private lateinit var clusterService: ClusterService

    private val searchHitParser = object : SearchResults.SearchHitParser<NotificationConfigInfo> {
        override fun parse(searchHit: SearchHit): NotificationConfigInfo {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                searchHit.sourceAsString
            )
            parser.nextToken()
            val doc = NotificationConfigDoc.parse(parser)
            return NotificationConfigInfo(
                searchHit.id,
                doc.metadata.lastUpdateTime,
                doc.metadata.createdTime,
                doc.metadata.tenant,
                doc.config
            )
        }
    }

    /**
     * Initialize the class
     * @param client The OpenSearch client
     * @param clusterService The OpenSearch cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService) {
        NotificationConfigIndex.client = SecureIndexClient(client)
        NotificationConfigIndex.clusterService = clusterService
    }

    /**
     * Create index using the mapping and settings defined in resource
     */
    @Suppress("TooGenericExceptionCaught")
    private fun createIndex() {
        if (!isIndexExists()) {
            val classLoader = NotificationConfigIndex::class.java.classLoader
            val indexMappingSource = classLoader.getResource(MAPPING_FILE_NAME)?.readText()!!
            val indexSettingsSource = classLoader.getResource(SETTINGS_FILE_NAME)?.readText()!!
            val request = CreateIndexRequest(INDEX_NAME)
                .mapping(MAPPING_TYPE, indexMappingSource, XContentType.YAML)
                .settings(indexSettingsSource, XContentType.YAML)
            try {
                val actionFuture = client.admin().indices().create(request)
                val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                if (response.isAcknowledged) {
                    log.info("$LOG_PREFIX:Index $INDEX_NAME creation Acknowledged")
                } else {
                    throw IllegalStateException("$LOG_PREFIX:Index $INDEX_NAME creation not Acknowledged")
                }
            } catch (exception: Exception) {
                if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                    throw exception
                }
            }
        }
    }

    /**
     * Check if the index is created and available.
     * @return true if index is available, false otherwise
     */
    private fun isIndexExists(): Boolean {
        val clusterState = clusterService.state()
        return clusterState.routingTable.hasIndex(INDEX_NAME)
    }

    /**
     * create a new doc for NotificationConfigDoc
     * @param configDoc the Notification Config Doc
     * @param id optional id to use as id for the document
     * @return Notification Config id if successful, null otherwise
     * @throws java.util.concurrent.ExecutionException with a cause
     */
    fun createNotificationConfig(configDoc: NotificationConfigDoc, id: String? = null): String? {
        createIndex()
        val indexRequest = IndexRequest(INDEX_NAME)
            .source(configDoc.toXContent())
            .create(true)
        if (id != null) {
            indexRequest.id(id)
        }
        val actionFuture = client.index(indexRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.result != DocWriteResponse.Result.CREATED) {
            log.warn("$LOG_PREFIX:createNotificationConfig - response:$response")
            null
        } else {
            response.id
        }
    }

    /**
     * Query index for Notification Config with ID
     * @param ids set of the document ids to get info
     * @return list of NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfigs(ids: Set<String>): List<NotificationConfigDocInfo> {
        createIndex()
        val getRequest = MultiGetRequest()
        ids.forEach { getRequest.add(INDEX_NAME, it) }
        val actionFuture = client.multiGet(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return response.responses.mapNotNull { parseNotificationConfigDoc(it.id, it.response) }
    }

    /**
     * Query index for Notification Config with ID
     * @param id the id for the document
     * @return NotificationConfigDocInfo on success, null otherwise
     */
    fun getNotificationConfig(id: String): NotificationConfigDocInfo? {
        createIndex()
        val getRequest = GetRequest(INDEX_NAME).id(id)
        val actionFuture = client.get(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return parseNotificationConfigDoc(id, response)
    }

    private fun parseNotificationConfigDoc(id: String, response: GetResponse): NotificationConfigDocInfo? {
        return if (response.sourceAsString == null) {
            log.warn("$LOG_PREFIX:getNotificationConfig - $id not found; response:$response")
            null
        } else {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString
            )
            parser.nextToken()
            val doc = NotificationConfigDoc.parse(parser)
            val info = DocInfo(
                id = id,
                version = response.version,
                seqNo = response.seqNo,
                primaryTerm = response.primaryTerm
            )
            NotificationConfigDocInfo(info, doc)
        }
    }

    /**
     * Query index for NotificationConfigDocs for given access details
     * @param tenant the tenant of the user
     * @param access the list of access details to search NotificationConfigDocs for.
     * @param request [GetNotificationConfigRequest] object
     * @return search result of NotificationConfigDocs
     */
    fun getAllNotificationConfigs(
        tenant: String,
        access: List<String>,
        request: GetNotificationConfigRequest
    ): NotificationConfigSearchResult {
        createIndex()
        val sourceBuilder = SearchSourceBuilder()
            .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
            .sort(getSortField(request.sortField), request.sortOrder ?: SortOrder.ASC)
            .size(request.maxItems)
            .from(request.fromIndex)
        val query = QueryBuilders.boolQuery()
        query.filter(QueryBuilders.termsQuery("$METADATA_TAG.$TENANT_TAG", tenant))
        if (access.isNotEmpty()) {
            query.filter(QueryBuilders.termsQuery("$METADATA_TAG.$ACCESS_LIST_TAG", access))
        }
        request.filterParams.forEach {
            query.filter(QueryHelper.getQueryBuilder(CONFIG_TAG, it.key, it.value))
        }
        sourceBuilder.query(query)
        val searchRequest = SearchRequest()
            .indices(INDEX_NAME)
            .source(sourceBuilder)
        val actionFuture = client.search(searchRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val result = NotificationConfigSearchResult(request.fromIndex.toLong(), response, searchHitParser)
        log.info(
            "$LOG_PREFIX:getAllNotificationConfigs from:${request.fromIndex}, maxItems:${request.maxItems}," +
                " sortField:${request.sortField}, sortOrder=${request.sortOrder}, filters=${request.filterParams}" +
                " retCount:${result.objectList.size}, totalCount:${result.totalHits}"
        )
        return result
    }

    /**
     * update NotificationConfigDoc for given id
     * @param id the id for the document
     * @param notificationConfigDoc the NotificationConfigDoc data
     * @return true if successful, false otherwise
     */
    fun updateNotificationConfig(id: String, notificationConfigDoc: NotificationConfigDoc): Boolean {
        createIndex()
        val updateRequest = UpdateRequest()
            .index(INDEX_NAME)
            .id(id)
            .doc(notificationConfigDoc.toXContent())
            .fetchSource(true)
        val actionFuture = client.update(updateRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.UPDATED) {
            log.warn("$LOG_PREFIX:updateNotificationConfig failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.UPDATED
    }

    /**
     * delete NotificationConfigDoc for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteNotificationConfig(id: String): Boolean {
        createIndex()
        val deleteRequest = DeleteRequest()
            .index(INDEX_NAME)
            .id(id)
        val actionFuture = client.delete(deleteRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.DELETED) {
            log.warn("$LOG_PREFIX:deleteNotificationConfig failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.DELETED
    }

    /**
     * delete NotificationConfigDoc for given ids
     * @param ids set of the document ids to delete
     * @return map of id to status
     */
    fun deleteNotificationConfigs(ids: Set<String>): Map<String, RestStatus> {
        createIndex()
        val bulkRequest = BulkRequest()
        ids.forEach {
            val deleteRequest = DeleteRequest()
                .index(INDEX_NAME)
                .id(it)
            bulkRequest.add(deleteRequest)
        }
        val actionFuture = client.bulk(bulkRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val mutableMap = mutableMapOf<String, RestStatus>()
        response.forEach {
            mutableMap[it.id] = it.status()
            if (it.isFailed) {
                log.warn("$LOG_PREFIX:deleteNotificationConfig failed for ${it.id}; response:${it.failureMessage}")
            }
        }
        return mutableMap
    }
}
