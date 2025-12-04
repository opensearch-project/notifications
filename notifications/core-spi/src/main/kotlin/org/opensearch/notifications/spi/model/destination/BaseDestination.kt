/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

/**
 * This class holds the generic parameters required for a BaseDestination.
 */
@SuppressWarnings("UnnecessaryAbstractClass")
abstract class BaseDestination(
    val destinationType: DestinationType,
)
