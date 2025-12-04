/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

/**
 * Enum representing email channel type.
 */
internal enum class EmailChannelType(
    val stringValue: String,
) {
    SMTP("smtp"),
    SES("ses"),
}
