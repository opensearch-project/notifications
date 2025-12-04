/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model

/**
 * Data class for storing destination message response per recipient.
 */
class DestinationMessageResponse(
    val statusCode: Int,
    val statusText: String,
)
