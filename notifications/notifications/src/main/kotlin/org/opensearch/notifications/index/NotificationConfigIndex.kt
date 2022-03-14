/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.index

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.get.MultiGetRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.index.IndexResponse
import org.opensearch.action.search.SearchRequest
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.NotificationConfigSearchResult
import org.opensearch.commons.notifications.model.SearchResults
import org.opensearch.commons.utils.logger
import org.opensearch.index.query.QueryBuilders
import org.opensearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.index.ConfigQueryHelper.getSortField
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata.Companion.ACCESS_LIST_TAG
import org.opensearch.notifications.model.DocMetadata.Companion.METADATA_TAG
import org.opensearch.notifications.model.NotificationConfigDoc
import org.opensearch.notifications.model.NotificationConfigDocInfo
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.notifications.util.SecureIndexClient
import org.opensearch.rest.RestStatus
import org.opensearch.search.SearchHit
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.SortOrder
import java.util.concurrent.TimeUnit
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.action.delete.DeleteResponse
import org.opensearch.action.get.MultiGetResponse
import org.opensearch.action.search.SearchResponse
import org.opensearch.action.support.master.AcknowledgedResponse

/**
 * Class for doing index operations to maintain configurations in cluster.
 */
@Suppress("TooManyFunctions")
internal object NotificationConfigIndex : ConfigOperations {
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
                doc.config
            )
        }
    }

    /**
     * {@inheritDoc}
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
            try {
                val deferred = mutableListOf<Deferred<AcknowledgedResponse>>()
                runBlocking {
                    deferred.add(
                        async(Dispatchers.IO) {
                            val request = CreateIndexRequest(INDEX_NAME)
                                .mapping(MAPPING_TYPE, indexMappingSource, XContentType.YAML)
                                .settings(indexSettingsSource, XContentType.YAML)
                            val actionFuture = client.admin().indices().create(request)
                            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                            response
                        }
                    )
                    deferred.awaitAll()
                }

                if (deferred.get(0).getCompleted().isAcknowledged) {
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
     * {@inheritDoc}
     */
    override fun createNotificationConfig(configDoc: NotificationConfigDoc, id: String?): String? {
        createIndex()
        val deferred = mutableListOf<Deferred<IndexResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val indexRequest = IndexRequest(INDEX_NAME)
                        .source(configDoc.toXContent())
                        .create(true)
                    if (id != null) {
                        indexRequest.id("$id")
                    }
                    val actionFuture = client.index(indexRequest)
                    val res = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    res
                }
            )
            deferred.awaitAll()
        }
        return if (deferred.get(0).getCompleted().result != DocWriteResponse.Result.CREATED) {
            log.warn("$LOG_PREFIX:createNotificationConfig - response:$deferred.get(0)")
            null
        } else {
            deferred.get(0).getCompleted().id
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getNotificationConfigs(ids: Set<String>): List<NotificationConfigDocInfo> {
        createIndex()
        val deferred = mutableListOf<Deferred<MultiGetResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val getRequest = MultiGetRequest()
                    ids.forEach { getRequest.add(INDEX_NAME, it) }
                    val actionFuture = client.multiGet(getRequest)
                    val res = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    res
                }
            )
            deferred.awaitAll()
        }

        val response : List<NotificationConfigDocInfo> = emptyList()

        response.map {
            deferred.forEach {
                it.getCompleted().responses.iterator().forEach {
                    parseNotificationConfigDoc(
                        it.id,
                        it.response
                    )
                }
            }
        }
        return response
    }

    /**
     * {@inheritDoc}
     */
    override fun getNotificationConfig(id: String): NotificationConfigDocInfo? {
        createIndex()
        val deferred = mutableListOf<Deferred<GetResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val getRequest = GetRequest(INDEX_NAME).id(id)
                    val actionFuture = client.get(getRequest)
                    val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    response
                }
            )
            deferred.awaitAll()
        }

        return parseNotificationConfigDoc(id, deferred.get(0).getCompleted())
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
     * {@inheritDoc}
     */
    override fun getAllNotificationConfigs(
        access: List<String>,
        request: GetNotificationConfigRequest
    ): NotificationConfigSearchResult {
        createIndex()
        val deferred = mutableListOf<Deferred<SearchResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val sourceBuilder = SearchSourceBuilder()
                        .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
                        .sort(getSortField(request.sortField), request.sortOrder ?: SortOrder.ASC)
                        .size(request.maxItems)
                        .from(request.fromIndex)
                    val query = QueryBuilders.boolQuery()
                    if (access.isNotEmpty()) {
                        query.filter(QueryBuilders.termsQuery("$METADATA_TAG.$ACCESS_LIST_TAG", access))
                    }
                    ConfigQueryHelper.addQueryFilters(query, request.filterParams)
                    sourceBuilder.query(query)
                    val searchRequest = SearchRequest()
                        .indices(INDEX_NAME)
                        .source(sourceBuilder)
                    val actionFuture = client.search(searchRequest)
                    val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    response
                }
            )
            deferred.awaitAll()
        }

        val result = NotificationConfigSearchResult(request.fromIndex.toLong(), deferred.get(0).getCompleted(), searchHitParser)
        log.info(
            "$LOG_PREFIX:getAllNotificationConfigs from:${request.fromIndex}, maxItems:${request.maxItems}," +
                " sortField:${request.sortField}, sortOrder=${request.sortOrder}, filters=${request.filterParams}" +
                " retCount:${result.objectList.size}, totalCount:${result.totalHits}"
        )
        return result
    }

    /**
     * {@inheritDoc}
     */
    override fun updateNotificationConfig(id: String, notificationConfigDoc: NotificationConfigDoc): Boolean {
        createIndex()
        val deferred = mutableListOf<Deferred<IndexResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val indexRequest = IndexRequest(INDEX_NAME)
                        .source(notificationConfigDoc.toXContent())
                        .create(false)
                        .id(id)
                    val actionFuture = client.index(indexRequest)
                    val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    response
                }
            )
            deferred.awaitAll()
        }
        if (deferred.get(0).getCompleted().result != DocWriteResponse.Result.UPDATED) {
            log.warn("$LOG_PREFIX:updateNotificationConfig failed for $id; response:$deferred.get(0).getCompleted()")
        }
        return deferred.get(0).getCompleted().result == DocWriteResponse.Result.UPDATED
    }

    /**
     * {@inheritDoc}
     */
    override fun deleteNotificationConfig(id: String): Boolean {
        createIndex()
        val deferred = mutableListOf<Deferred<DeleteResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val deleteRequest = DeleteRequest()
                        .index(INDEX_NAME)
                        .id(id)
                    val actionFuture = client.delete(deleteRequest)
                    val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    response
                }
            )
            deferred.awaitAll()
        }

        if (deferred.get(0).getCompleted().result != DocWriteResponse.Result.DELETED) {
            log.warn("$LOG_PREFIX:deleteNotificationConfig failed for $id; response:$deferred.get(0).getCompleted()")
        }
        return deferred.get(0).getCompleted().result == DocWriteResponse.Result.DELETED
    }

    /**
     * {@inheritDoc}
     */
    override fun deleteNotificationConfigs(ids: Set<String>): Map<String, RestStatus> {
        createIndex()

        val deferred = mutableListOf<Deferred<BulkResponse>>()
        runBlocking {
            deferred.add(
                async(Dispatchers.IO) {
                    val bulkRequest = BulkRequest()
                    ids.forEach {
                        val deleteRequest = DeleteRequest()
                            .index(INDEX_NAME)
                            .id(it)
                        bulkRequest.add(deleteRequest)
                    }
                    val actionFuture = client.bulk(bulkRequest)
                    val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                    response
                }
            )
            deferred.awaitAll()
        }

        val mutableMap = mutableMapOf<String, RestStatus>()

        deferred.get(0).getCompleted().forEach {
            mutableMap[it.id] = it.status()
            if (it.isFailed) {
                log.warn("$LOG_PREFIX:deleteNotificationConfig failed for ${it.id}; response:${it.failureMessage}")
            }
        }
        return mutableMap
    }
}
