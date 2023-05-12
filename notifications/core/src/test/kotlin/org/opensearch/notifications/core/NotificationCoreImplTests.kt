/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
        "email_group",
        "microsoft_teams"
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
        assertEquals(defaultConfigTypes, NotificationCoreImpl.getAllowedConfigTypes())
        assertEquals(defaultPluginFeatures, NotificationCoreImpl.getPluginFeatures())
    }
}
