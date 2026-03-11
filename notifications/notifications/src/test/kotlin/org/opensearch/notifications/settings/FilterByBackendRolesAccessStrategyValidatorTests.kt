
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.settings

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class FilterByBackendRolesAccessStrategyValidatorTests {
    @Test
    fun `accepts valid values`() {
        val validator = FilterByBackendRolesAccessStrategyValidator()
        validator.validate(FilterByBackendRolesAccessStrategy.INTERSECT.strategy)
        validator.validate(FilterByBackendRolesAccessStrategy.ALL.strategy)
    }

    @Test
    fun `rejects invalid value`() {
        val validator = FilterByBackendRolesAccessStrategyValidator()

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate("foo")
        }
    }
}
