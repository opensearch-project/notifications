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

/**
 * Defines a generic counter.
 */
interface Counter<T> {
    /** Increments the count value by 1 unit */
    fun increment()

    /** Increments the count value by n unit */
    fun add(n: Long)

    /** Retrieves the count value accumulated up to this call */
    fun getValue(): Long

    /** Resets the count value to initial value when Counter is created */
    fun reset()
}
