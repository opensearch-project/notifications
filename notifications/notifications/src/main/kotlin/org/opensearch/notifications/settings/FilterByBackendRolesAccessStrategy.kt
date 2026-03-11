/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

/**
 * Defines the FilterByBackendRolesAccessStrategy
 */
internal enum class FilterByBackendRolesAccessStrategy(val strategy: String) {
    /**
     * Backend roles must intersect to have access
     */
    INTERSECT("intersect"),

    /**
     * Backend roles must be exactly equal to have access
     */
    ALL("all")
}
