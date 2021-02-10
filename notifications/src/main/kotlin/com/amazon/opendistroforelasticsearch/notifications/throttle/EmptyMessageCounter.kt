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

import java.util.Date

/**
 * Empty implementation of the message counter which responds with IllegalStateException all operations.
 */
internal object EmptyMessageCounter : MessageCounter {
    /**
     * {@inheritDoc}
     */
    override fun incrementCountersForDay(counterDay: Date, counters: Counters) {
        throw IllegalStateException("MessageCounter not initialized")
    }

    /**
     * {@inheritDoc}
     */
    override fun getCounterForMonth(counterDay: Date): Counters {
        throw IllegalStateException("MessageCounter not initialized")
    }
}
