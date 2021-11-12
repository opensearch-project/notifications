/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model

import org.opensearch.common.settings.SecureString

data class SecureDestinationSettings(val emailUsername: SecureString, val emailPassword: SecureString)
