/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput

/**
 * Request object for the re-encrypt-all-configs admin operation.
 * No parameters are required — the endpoint re-encrypts every stored
 * notification-channel config with the current active key.
 */
class ReencryptNotificationConfigsRequest() : ActionRequest() {

    constructor(sin: StreamInput) : this()

    override fun validate(): ActionRequestValidationException? = null

    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
    }
}
