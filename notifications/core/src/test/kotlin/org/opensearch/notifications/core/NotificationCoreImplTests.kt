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

package org.opensearch.notifications.core

import org.junit.jupiter.api.Test
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_ALLOWED_CONFIG_FEATURES
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_ALLOWED_CONFIG_TYPES
import org.opensearch.notifications.core.setting.PluginSettings.DEFAULT_TOOLTIP_SUPPORT
import kotlin.test.assertEquals

class NotificationCoreImplTests {

    private val defaultPluginFeatures = mapOf(
        "tooltip_support" to DEFAULT_TOOLTIP_SUPPORT.toString()
    )

    @Test
    fun `test all get configs APIs return the default value`() {
        assertEquals(DEFAULT_ALLOWED_CONFIG_FEATURES, NotificationCoreImpl.getAllowedConfigFeatures())
        assertEquals(DEFAULT_ALLOWED_CONFIG_TYPES, NotificationCoreImpl.getAllowedConfigTypes())
        assertEquals(defaultPluginFeatures, NotificationCoreImpl.getPluginFeatures())
    }
}
