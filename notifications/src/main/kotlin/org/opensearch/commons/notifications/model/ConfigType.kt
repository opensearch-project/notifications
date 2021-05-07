/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.opensearch.commons.notifications.model

import org.opensearch.commons.utils.EnumParser

/**
 * Enum for Notification config type
 */
enum class ConfigType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    SLACK("slack") {
        override fun toString(): String {
            return tag
        }
    },
    CHIME("chime") {
        override fun toString(): String {
            return tag
        }
    },
    WEBHOOK("webhook") {
        override fun toString(): String {
            return tag
        }
    },
    EMAIL("email") {
        override fun toString(): String {
            return tag
        }
    },
    SMTP_ACCOUNT("smtp_account") {
        override fun toString(): String {
            return tag
        }
    },
    EMAIL_GROUP("email_group") {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get ConfigType from tag or NONE if not found
         * @param tag the tag
         * @return ConfigType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): ConfigType {
            return tagMap[tag] ?: NONE
        }
    }
}
