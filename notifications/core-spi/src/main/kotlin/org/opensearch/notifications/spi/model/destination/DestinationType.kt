/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

/**
 * Supported notification destinations
 */
enum class DestinationType {
    CHIME,
    SLACK,
    MICROSOFT_TEAMS,
    CUSTOM_WEBHOOK,
    SMTP,
    SES,
    SNS,
}
