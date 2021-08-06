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

package org.opensearch.notifications.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * Counter to hold accumulative value over time.
 */
public class BasicCounter implements Counter<Long> {
    private final LongAdder count = new LongAdder();

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        count.increment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long n) {
        count.add(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getValue() {
        return count.longValue();
    }

    /** Reset the count value to zero*/
    @Override
    public void reset() {
        count.reset();
    }
}
