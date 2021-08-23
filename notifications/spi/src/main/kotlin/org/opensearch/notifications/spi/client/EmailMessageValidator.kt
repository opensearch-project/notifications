/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package org.opensearch.notifications.spi.client

import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.setting.PluginSettings
import org.opensearch.notifications.spi.utils.logger

/**
 * This class handles the connections to the given Destination.
 */
internal object EmailMessageValidator {
    private val log by logger(EmailMessageValidator::class.java)
    fun isMessageSizeOverLimit(message: MessageContent): Boolean {
        val approxAttachmentLength = if (message.fileData != null && message.fileName != null) {
            PluginSettings.emailMinimumHeaderLength + message.fileData.length + message.fileName.length
        } else {
            0
        }

        val approxEmailLength = PluginSettings.emailMinimumHeaderLength +
            message.title.length +
            message.textDescription.length +
            (message.htmlDescription?.length ?: 0) +
            approxAttachmentLength

        return approxEmailLength > PluginSettings.emailSizeLimit
    }
}
