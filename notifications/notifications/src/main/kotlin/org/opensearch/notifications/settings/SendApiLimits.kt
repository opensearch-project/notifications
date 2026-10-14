/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

/**
 * What the send API accepts from a caller, from the send_api settings in [PluginSettings].
 */
internal data class SendApiLimits(
    val maxTitleLength: Int,
    val maxTextLength: Int,
    val allowHtmlAndAttachments: Boolean
)
