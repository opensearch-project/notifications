package org.opensearch.commons.notifications.model

import org.opensearch.commons.utils.EnumParser

enum class MethodType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    SSL("ssl") {
        override fun toString(): String {
            return tag
        }
    },
    START_TLS("start_tls") {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get MethodType from tag or NONE if not found
         * @param tag the tag
         * @return MethodType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): MethodType {
            return tagMap[tag] ?: NONE
        }
    }
}
