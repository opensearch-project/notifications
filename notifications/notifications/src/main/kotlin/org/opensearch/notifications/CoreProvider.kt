/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications

import org.opensearch.notifications.spi.NotificationCore

internal object CoreProvider {
    lateinit var core: NotificationCore

    fun initialize(core: NotificationCore) {
        this.core = core
    }
}
