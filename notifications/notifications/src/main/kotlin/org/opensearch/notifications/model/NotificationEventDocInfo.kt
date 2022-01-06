/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

/**
 * Class to hold notification event document with information
 */
data class NotificationEventDocInfo(
    val docInfo: DocInfo,
    val eventDoc: NotificationEventDoc
)
