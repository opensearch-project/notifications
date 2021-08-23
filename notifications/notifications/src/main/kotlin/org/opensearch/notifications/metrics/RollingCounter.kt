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
package org.opensearch.notifications.metrics

import java.time.Clock
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.jvm.JvmOverloads

/**
 * Rolling counter. The count is refreshed every interval. In every interval the count is cumulative.
 */
class RollingCounter @JvmOverloads constructor(
    private val window: Long = METRICS_ROLLING_WINDOW_VALUE,
    private val interval: Long = METRICS_ROLLING_INTERVAL_VALUE,
    private val clock: Clock = Clock.systemDefaultZone()
) : Counter<Long?> {
    private val capacity: Long = window / interval * 2
    private val timeToCountMap = ConcurrentSkipListMap<Long, Long>()

    /**
     * {@inheritDoc}
     */
    override fun increment() {
        add(1L)
    }

    /**
     * {@inheritDoc}
     */
    override fun add(n: Long) {
        trim()
        timeToCountMap.compute(getKey(clock.millis())) { k: Long?, v: Long? -> if (v == null) n else v + n }
    }

    /**
     * {@inheritDoc}
     */
    override fun getValue(): Long {
        return getValue(getPreKey(clock.millis()))
    }

    /**
     * {@inheritDoc}
     */
    fun getValue(key: Long): Long {
        return timeToCountMap[key] ?: return 0
    }

    private fun trim() {
        if (timeToCountMap.size > capacity) {
            timeToCountMap.headMap(getKey(clock.millis() - window * 1000)).clear()
        }
    }

    private fun getKey(millis: Long): Long {
        return millis / 1000 / interval
    }

    private fun getPreKey(millis: Long): Long {
        return getKey(millis) - 1
    }

    /**
     * Number of existing intervals
     */
    fun size(): Int {
        return timeToCountMap.size
    }

    /**
     * Remove all the items from counter
     */
    override fun reset() {
        timeToCountMap.clear()
    }

    companion object {
        private const val METRICS_ROLLING_WINDOW_VALUE = 3600L
        private const val METRICS_ROLLING_INTERVAL_VALUE = 60L
    }
}
