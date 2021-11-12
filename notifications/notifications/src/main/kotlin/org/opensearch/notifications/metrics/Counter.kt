/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
