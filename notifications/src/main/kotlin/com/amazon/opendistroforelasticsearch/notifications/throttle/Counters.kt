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

import java.util.concurrent.atomic.AtomicInteger

/**
 * Counter class to maintain the counting of the items
 */
internal class Counters {
    /**
     * Number of requests.
     */
    val requestCount = AtomicInteger()

    /**
     * Number of email sent successfully
     */
    val emailSentSuccessCount = AtomicInteger()

    /**
     * Number of email request failed
     */
    val emailSentFailureCount = AtomicInteger()

    /**
     * Increment the counters by given counter values
     * @param counters The counter values to increment
     */
    fun incrementCountersBy(counters: Counters) {
        requestCount.addAndGet(counters.requestCount.get())
        emailSentSuccessCount.addAndGet(counters.emailSentSuccessCount.get())
        emailSentFailureCount.addAndGet(counters.emailSentFailureCount.get())
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return "{requestCount=$requestCount, emailSentSuccessCount=$emailSentSuccessCount, emailSentFailureCount=$emailSentFailureCount}"
    }
}
