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

import com.amazon.opendistroforelasticsearch.notifications.NotificationPlugin.Companion.PLUGIN_NAME
import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import com.amazon.opendistroforelasticsearch.notifications.throttle.CounterIndexModel.Companion.getIdForDate
import org.apache.logging.log4j.LogManager
import org.elasticsearch.ResourceAlreadyExistsException
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.service.ClusterService
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.seqno.SequenceNumbers
import java.util.Date

/**
 * Class for doing ES index operation to maintain counters in cluster.
 */
internal class CounterIndex(val client: Client, val clusterService: ClusterService) : MessageCounter {
    private val log = LogManager.getLogger(javaClass)

    companion object {
        private const val COUNTER_INDEX_NAME = ".opendistro-notifications-counter"
        private const val COUNTER_INDEX_SCHEMA_FILE_NAME = "opendistro-notifications-counter.yml"
        private const val MAPPING_TYPE = "_doc"
    }

    /**
     * {@inheritDoc}
     */
    override fun getCounterForMonth(counterDay: Date): Counters {
        TODO("Not yet implemented")
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
                log.info("$PLUGIN_NAME:VersionConflictEngineException retrying")
                false
            } catch (ignored: DocumentMissingException) {
                log.info("$PLUGIN_NAME:DocumentMissingException retrying")
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
        val request = CreateIndexRequest(COUNTER_INDEX_NAME)
            .mapping(MAPPING_TYPE, indexMappingSource, XContentType.YAML)
        try {
            val actionFuture = client.admin().indices().create(request)
            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
            if (response.isAcknowledged) {
                log.info("$PLUGIN_NAME:Index $COUNTER_INDEX_NAME creation Acknowledged")
            } else {
                throw IllegalStateException("$PLUGIN_NAME:Index $COUNTER_INDEX_NAME creation not Acknowledged")
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
        log.debug("$PLUGIN_NAME:getCounterIndexFor returned ${response.seqNo}:${response.primaryTerm}::${response.sourceAsString}")
        return if (response.sourceAsString == null) {
            CounterIndexModel(counterDay, 0, 0, 0)
        } else {
            val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString)
            parser.nextToken()
            var retValue = CounterIndexModel.parse(parser, response.seqNo, response.primaryTerm)
            if (getIdForDate(retValue.counterDay).equals(getIdForDate(counterDay))) {
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
        log.info("$PLUGIN_NAME:CounterIndex createCounterIndex - $counters status:${response.result}")
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
        log.info("$PLUGIN_NAME:CounterIndex updateCounterIndex - $counterIndexModel status:${response.result}")
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
        log.info("$PLUGIN_NAME:CounterIndex currentValue - $currentValue")
        return if (currentValue.seqNo == SequenceNumbers.UNASSIGNED_SEQ_NO) {
            createCounterIndexFor(counterDay, counters)
        } else {
            updateCounterIndexFor(counterDay, currentValue.copyAndIncrementBy(counters))
        }
    }
}
