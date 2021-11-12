/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

/**
 * Class to hold document with information
 */
data class NotificationConfigDocInfo(
    val docInfo: DocInfo,
    val configDoc: NotificationConfigDoc
)
