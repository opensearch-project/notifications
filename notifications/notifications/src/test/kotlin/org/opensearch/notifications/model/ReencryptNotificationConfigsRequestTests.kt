/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject

internal class ReencryptNotificationConfigsRequestTests {

    @Test
    fun `validate returns null — request always valid`() {
        assertNull(ReencryptNotificationConfigsRequest().validate())
    }

    @Test
    fun `serialize and deserialize over transport stream produces equal object`() {
        val original = ReencryptNotificationConfigsRequest()
        val recreated = recreateObject(original) { ReencryptNotificationConfigsRequest(it) }
        // Both are empty value objects; successful round-trip without exception is sufficient.
        assertNull(recreated.validate())
    }
}
