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

/**
 * Defines a generic counter.
 */
public interface Counter<T> {

    /** Increments the count value by 1 unit*/
    void increment();

    /** Increments the count value by n unit*/
    void add(long n);

    /** Retrieves the count value accumulated upto this call*/
    T getValue();

    /** Resets the count value to initial value when Counter is created*/
    void reset();
}
