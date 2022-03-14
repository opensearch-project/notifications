/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.index

import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.ActionListener
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.delete.DeleteResponse
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.get.MultiGetRequest
import org.opensearch.action.get.MultiGetResponse
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.index.IndexResponse
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.search.SearchResponse
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentHelper
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

/**
 * Class for doing index operations to maintain configurations in cluster.
 */
@Suppress("TooManyFunctions")
internal object NotificationConfigIndex : ConfigOperations {
    private val log by logger(NotificationConfigIndex::class.java)
    private const val INDEX_NAME = ".opensearch-notifications-config"
    private const val MAPPING_FILE_NAME = "notifications-config-mapping.yml"
    private const val SETTINGS_FILE_NAME = "notifications-config-settings.yml"

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
            val indexMappingAsMap = XContentHelper.convertToMap(XContentType.YAML.xContent(), indexMappingSource, false)
            val indexSettingsSource = classLoader.getResource(SETTINGS_FILE_NAME)?.readText()!!
            val request = CreateIndexRequest(INDEX_NAME)
                .mapping(indexMappingAsMap)
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
     * {@inheritDoc}
     */
    override fun createNotificationConfig(
        configDoc: NotificationConfigDoc,
        id: String?,
        actionListener: ActionListener<String>
    ) {
        createIndex()
        val indexRequest = IndexRequest(INDEX_NAME)
            .source(configDoc.toXContent())
            .create(true)
        if (id != null) {
            indexRequest.id(id)
        }
        client.index(
            indexRequest,
            object : ActionListener<IndexResponse> {
                override fun onResponse(response: IndexResponse) {
                    if (response.result != DocWriteResponse.Result.CREATED) {
                        log.warn("$LOG_PREFIX:createNotificationConfig - response:$response")
                        actionListener.onResponse(null)
                    } else {
                        actionListener.onResponse(response.id)
                    }
                }
                override fun onFailure(exception: Exception) {
                    log.error("$LOG_PREFIX: Error in createNotificationConfig - response:${exception.message}")
                    actionListener.onResponse(null)
                }
            }
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getNotificationConfigs(
        ids: Set<String>,
        actionListener: ActionListener<List<NotificationConfigDocInfo>>
    ) {
        createIndex()
        val getRequest = MultiGetRequest()
        ids.forEach { getRequest.add(INDEX_NAME, it) }
        client.multiGet(
            getRequest,
            object : ActionListener<MultiGetResponse> {
                override fun onResponse(response: MultiGetResponse) {
                    actionListener.onResponse(
                        response.responses.mapNotNull {
                            parseNotificationConfigDoc(it.id, it.response)
                        }
                    )
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getNotificationConfig(id: String, actionListener: ActionListener<NotificationConfigDocInfo>) {
        createIndex()
        val getRequest = GetRequest(INDEX_NAME).id(id)
        client.get(
            getRequest,
            object : ActionListener<GetResponse> {
                override fun onResponse(response: GetResponse) {
                    actionListener.onResponse(parseNotificationConfigDoc(id, response))
                }
                override fun onFailure(ex: Exception) {
                    actionListener.onFailure(ex)
                }
            }
        )
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
        request: GetNotificationConfigRequest,
        actionListener: ActionListener<NotificationConfigSearchResult>
    ) {
        createIndex()
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

        client.search(
            searchRequest,
            object : ActionListener<SearchResponse> {
                override fun onResponse(response: SearchResponse) {
                    val result = NotificationConfigSearchResult(request.fromIndex.toLong(), response, searchHitParser)
                    log.info(
                        "$LOG_PREFIX:getAllNotificationConfigs from:${request.fromIndex}, maxItems:${request.maxItems}," +
                            " sortField:${request.sortField}, sortOrder=${request.sortOrder}, filters=${request.filterParams}" +
                            " retCount:${result.objectList.size}, totalCount:${result.totalHits}"
                    )
                    actionListener.onResponse(result)
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun updateNotificationConfig(
        id: String,
        notificationConfigDoc: NotificationConfigDoc,
        actionListener: ActionListener<Boolean>
    ) {
        createIndex()
        val indexRequest = IndexRequest(INDEX_NAME)
            .source(notificationConfigDoc.toXContent())
            .create(false)
            .id(id)
        client.index(
            indexRequest,
            object : ActionListener<IndexResponse> {
                override fun onResponse(response: IndexResponse) {
                    if (response.result != DocWriteResponse.Result.UPDATED) {
                        log.warn("$LOG_PREFIX:updateNotificationConfig failed for $id; response:$response")
                    }
                    actionListener.onResponse(response.result == DocWriteResponse.Result.UPDATED)
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun deleteNotificationConfig(id: String, actionListener: ActionListener<Boolean>) {
        createIndex()
        val deleteRequest = DeleteRequest()
            .index(INDEX_NAME)
            .id(id)
        client.delete(
            deleteRequest,
            object : ActionListener<DeleteResponse> {
                override fun onResponse(response: DeleteResponse) {
                    if (response.result != DocWriteResponse.Result.DELETED) {
                        log.warn("$LOG_PREFIX:deleteNotificationConfig failed for $id; response:$response")
                    }
                    actionListener.onResponse(response.result == DocWriteResponse.Result.DELETED)
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun deleteNotificationConfigs(ids: Set<String>, actionListener: ActionListener<Map<String, RestStatus>>) {
        createIndex()
        val bulkRequest = BulkRequest()
        ids.forEach {
            val deleteRequest = DeleteRequest()
                .index(INDEX_NAME)
                .id(it)
            bulkRequest.add(deleteRequest)
        }
        client.bulk(
            bulkRequest,
            object : ActionListener<BulkResponse> {
                override fun onResponse(response: BulkResponse) {
                    val mutableMap = mutableMapOf<String, RestStatus>()
                    response.forEach {
                        mutableMap[it.id] = it.status()
                        if (it.isFailed) {
                            log.warn("$LOG_PREFIX:deleteNotificationConfig failed for ${it.id}; response:${it.failureMessage}")
                        }
                    }
                    actionListener.onResponse(mutableMap)
                }
                override fun onFailure(exception: Exception) {
                    actionListener.onFailure(exception)
                }
            }
        )
    }
}
