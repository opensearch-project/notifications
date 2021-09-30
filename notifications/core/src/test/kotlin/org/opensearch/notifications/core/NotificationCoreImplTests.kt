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
import kotlin.test.assertEquals

class NotificationCoreImplTests {

    private val defaultTooltipSupport = true
    private val defaultConfigTypes = listOf(
        "slack",
        "chime",
        "webhook",
        "email",
        "sns",
        "ses_account",
        "smtp_account",
        "email_group"
    )
    private val defaultConfigFeatures = listOf(
        "alerting",
        "index_management",
        "reports"
    )

    private val defaultPluginFeatures = mapOf(
        "tooltip_support" to defaultTooltipSupport.toString()
    )

    @Test
    fun `test all get configs APIs return the default value`() {
        assertEquals(defaultConfigTypes, NotificationCoreImpl.getAllowedConfigFeatures())
        assertEquals(defaultConfigFeatures, NotificationCoreImpl.getAllowedConfigTypes())
        assertEquals(defaultPluginFeatures, NotificationCoreImpl.getPluginFeatures())
    }
}
