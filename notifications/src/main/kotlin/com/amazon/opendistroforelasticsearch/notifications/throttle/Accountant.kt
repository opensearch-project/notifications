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

import com.amazon.opendistroforelasticsearch.notifications.settings.PluginSettings
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.service.ClusterService
import java.util.Date

/**
 * The object class for keep track of the messages sent and provide throttle data.
 */
internal object Accountant {
    private var messageCounter: MessageCounter = EmptyMessageCounter

    /**
     * Initialize the class
     * @param client The ES client
     * @param clusterService The ES cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService) {
        this.messageCounter = CounterIndex(client, clusterService)
    }

    /**
     * Increment the counters by provided value
     * @param counters the counter object
     */
    fun incrementCounters(counters: Counters) {
        messageCounter.incrementCountersForDay(Date(), counters)
    }

    /**
     * Check if message quota is available
     * @param counters the counter object
     * @return true if message quota is available, false otherwise
     */
    fun isMessageQuotaAvailable(counters: Counters): Boolean {
        val monthlyCounters = messageCounter.getCounterForMonth(Date())
        monthlyCounters.incrementCountersBy(counters)
        return monthlyCounters.emailSentSuccessCount.get() <= PluginSettings.emailMonthlyLimit
    }
}
