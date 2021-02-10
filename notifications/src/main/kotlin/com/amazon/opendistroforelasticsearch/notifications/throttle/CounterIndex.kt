/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.notifications.throttle

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import com.amazon.opendistroforelasticsearch.notifications.throttle.CounterIndexModel.Companion.COUNTER_INDEX_MODEL_KEY
import com.amazon.opendistroforelasticsearch.notifications.throttle.CounterIndexModel.Companion.MAX_ITEMS_IN_MONTH
import com.amazon.opendistroforelasticsearch.notifications.throttle.CounterIndexModel.Companion.getIdForDate
import com.amazon.opendistroforelasticsearch.notifications.throttle.CounterIndexModel.Companion.getIdForStartOfMonth
import com.amazon.opendistroforelasticsearch.notifications.util.SecureIndexClient
import com.amazon.opendistroforelasticsearch.notifications.util.logger
import org.elasticsearch.ResourceAlreadyExistsException
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.service.ClusterService
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.seqno.SequenceNumbers
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Class for doing ES index operation to maintain counters in cluster.
 */
internal class CounterIndex(client: Client, private val clusterService: ClusterService) : MessageCounter {
    private val client: Client

    init {
        this.client = SecureIndexClient(client)
    }

    internal companion object {
        private val log by logger(CounterIndex::class.java)
        private const val COUNTER_INDEX_NAME = ".opendistro-notifications-counter"
        private const val COUNTER_INDEX_SCHEMA_FILE_NAME = "opendistro-notifications-counter.yml"
        private const val COUNTER_INDEX_SETTINGS_FILE_NAME = "opendistro-notifications-counter-settings.yml"
        private const val MAPPING_TYPE = "_doc"
    }

    /**
     * {@inheritDoc}
     */
    override fun getCounterForMonth(counterDay: Date): Counters {
        val retValue = Counters()
        if (!isIndexExists()) {
            createIndex()
        } else {
            val startDay = getIdForStartOfMonth(counterDay)
            val currentDay = getIdForDate(counterDay)
            val query = QueryBuilders.rangeQuery(COUNTER_INDEX_MODEL_KEY).gte(startDay).lte(currentDay)
            val sourceBuilder = SearchSourceBuilder()
                .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
                .size(MAX_ITEMS_IN_MONTH)
                .from(0)
                .query(query)
            val searchRequest = SearchRequest()
                .indices(COUNTER_INDEX_NAME)
                .source(sourceBuilder)
            val actionFuture = client.search(searchRequest)
            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
            response.hits.forEach {
                val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY,
                    LoggingDeprecationHandler.INSTANCE,
                    it.sourceAsString)
                parser.nextToken()
                val modelValues = CounterIndexModel.parse(parser)
                retValue.requestCount.addAndGet(modelValues.requestCount)
                retValue.emailSentSuccessCount.addAndGet(modelValues.emailSentSuccessCount)
                retValue.emailSentFailureCount.addAndGet(modelValues.emailSentFailureCount)
            }
            log.info("$LOG_PREFIX:getCounterForMonth:$retValue")
        }
        return retValue
    }

    /**
     * {@inheritDoc}
     */
    @Suppress("TooGenericExceptionCaught")
    override fun incrementCountersForDay(counterDay: Date, counters: Counters) {
        if (!isIndexExists()) {
            createIndex()
        }
        var isIncremented = false
        while (!isIncremented) {
            isIncremented = try {
                incrementCounterIndexFor(counterDay, counters)
            } catch (ignored: VersionConflictEngineException) {
                log.info("$LOG_PREFIX:VersionConflictEngineException retrying")
                false
            } catch (ignored: DocumentMissingException) {
                log.info("$LOG_PREFIX:DocumentMissingException retrying")
                false
            } catch (exception: Exception) {
                throw exception
            }
        }
    }

    /**
     * Create index using the schema defined in resource
     */
    @Suppress("TooGenericExceptionCaught")
    private fun createIndex() {
        val indexMappingSource = CounterIndex::class.java.classLoader.getResource(COUNTER_INDEX_SCHEMA_FILE_NAME)?.readText()!!
        val indexSettingsSource = CounterIndex::class.java.classLoader.getResource(COUNTER_INDEX_SETTINGS_FILE_NAME)?.readText()!!
        val request = CreateIndexRequest(COUNTER_INDEX_NAME)
            .mapping(MAPPING_TYPE, indexMappingSource, XContentType.YAML)
            .settings(indexSettingsSource, XContentType.YAML)
        try {
            val actionFuture = client.admin().indices().create(request)
            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
            if (response.isAcknowledged) {
                log.info("$LOG_PREFIX:Index $COUNTER_INDEX_NAME creation Acknowledged")
            } else {
                throw IllegalStateException("$LOG_PREFIX:Index $COUNTER_INDEX_NAME creation not Acknowledged")
            }
        } catch (exception: Exception) {
            if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                throw exception
            }
        }
    }

    /**
     * Check if the index is created and available.
     * @return true if index is available, false otherwise
     */
    private fun isIndexExists(): Boolean {
        val clusterState = clusterService.state()
        return clusterState.routingTable.hasIndex(COUNTER_INDEX_NAME)
    }

    /**
     * Query index for counter for given day
     * @param counterDay the counter day
     * @return counter index model
     */
    private fun getCounterIndexFor(counterDay: Date): CounterIndexModel {
        val getRequest = GetRequest(COUNTER_INDEX_NAME).id(getIdForDate(counterDay))
        val actionFuture = client.get(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.sourceAsString == null) {
            CounterIndexModel(counterDay, 0, 0, 0)
        } else {
            val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString)
            parser.nextToken()
            val retValue = CounterIndexModel.parse(parser, response.seqNo, response.primaryTerm)
            if (getIdForDate(retValue.counterDay) == getIdForDate(counterDay)) {
                CounterIndexModel(counterDay, 0, 0, 0)
            }
            retValue
        }
    }

    /**
     * create a new doc for counter for given day
     * @param counterDay the counter day
     * @param counters the initial counter values
     * @return true if successful, false otherwise
     */
    private fun createCounterIndexFor(counterDay: Date, counters: Counters): Boolean {
        val indexRequest = IndexRequest(COUNTER_INDEX_NAME)
            .id(getIdForDate(counterDay))
            .source(CounterIndexModel.getCounterIndexModel(counterDay, counters).toXContent())
            .setIfSeqNo(SequenceNumbers.UNASSIGNED_SEQ_NO)
            .setIfPrimaryTerm(SequenceNumbers.UNASSIGNED_PRIMARY_TERM)
            .create(true)
        val actionFuture = client.index(indexRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        log.debug("$LOG_PREFIX:CounterIndex createCounterIndex - $counters status:${response.result}")
        return response.result == DocWriteResponse.Result.CREATED
    }

    /**
     * update existing doc for counter for given day
     * @param counterDay the counter day
     * @param counterIndexModel the counter index to update
     * @return true if successful, false otherwise
     */
    private fun updateCounterIndexFor(counterDay: Date, counterIndexModel: CounterIndexModel): Boolean {
        val updateRequest = UpdateRequest()
            .index(COUNTER_INDEX_NAME)
            .id(getIdForDate(counterDay))
            .setIfSeqNo(counterIndexModel.seqNo)
            .setIfPrimaryTerm(counterIndexModel.primaryTerm)
            .doc(counterIndexModel.toXContent())
            .fetchSource(true)
        val actionFuture = client.update(updateRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        log.debug("$LOG_PREFIX:CounterIndex updateCounterIndex - $counterIndexModel status:${response.result}")
        return response.result == DocWriteResponse.Result.UPDATED
    }

    /**
     * create or update doc with counter added to existing value
     * @param counterDay the counter day
     * @param counters the counter values to increment
     * @return true if successful, false otherwise
     */
    private fun incrementCounterIndexFor(counterDay: Date, counters: Counters): Boolean {
        val currentValue = getCounterIndexFor(counterDay)
        log.debug("$LOG_PREFIX:CounterIndex currentValue - $currentValue")
        return if (currentValue.seqNo == SequenceNumbers.UNASSIGNED_SEQ_NO) {
            createCounterIndexFor(counterDay, counters)
        } else {
            updateCounterIndexFor(counterDay, currentValue.copyAndIncrementBy(counters))
        }
    }
}
