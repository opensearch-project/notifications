/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi

/**
 * SPI of Notification Core
 */
interface NotificationCoreExtension {
    /**
     * @param core Set notification core
     */
    fun setNotificationCore(core: NotificationCore)
}
