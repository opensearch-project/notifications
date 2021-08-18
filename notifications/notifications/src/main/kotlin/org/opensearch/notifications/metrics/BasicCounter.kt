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

import java.util.concurrent.atomic.LongAdder

/**
 * Counter to hold accumulative value over time.
 */
class BasicCounter : Counter<Long?> {
    private val count = LongAdder()

    /**
     * {@inheritDoc}
     */
    override fun increment() {
        count.increment()
    }

    /**
     * {@inheritDoc}
     */
    override fun add(n: Long) {
        count.add(n)
    }

    /**
     * {@inheritDoc}
     */
    override fun getValue(): Long {
        return count.toLong()
    }

    /** Reset the count value to zero */
    override fun reset() {
        count.reset()
    }
}
