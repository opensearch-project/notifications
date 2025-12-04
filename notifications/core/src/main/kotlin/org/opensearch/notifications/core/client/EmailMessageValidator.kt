/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

import org.opensearch.notifications.core.setting.PluginSettings
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.MessageContent

/**
 * This class handles the connections to the given Destination.
 */
internal object EmailMessageValidator {
    private val log by logger(EmailMessageValidator::class.java)

    fun isMessageSizeOverLimit(message: MessageContent): Boolean {
        val approxAttachmentLength =
            if (message.fileData != null && message.fileName != null) {
                PluginSettings.emailMinimumHeaderLength + message.fileData!!.length + message.fileName!!.length
            } else {
                0
            }

        val approxEmailLength =
            PluginSettings.emailMinimumHeaderLength +
                message.title.length +
                message.textDescription.length +
                (message.htmlDescription?.length ?: 0) +
                approxAttachmentLength

        return approxEmailLength > PluginSettings.emailSizeLimit
    }
}
