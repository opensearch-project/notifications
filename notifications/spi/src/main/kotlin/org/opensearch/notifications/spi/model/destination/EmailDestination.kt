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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.notifications.spi.model.destination

import org.opensearch.common.Strings
import org.opensearch.notifications.spi.utils.validateEmail

/**
 * This class holds the contents of email destination
 */
class EmailDestination(
    val host: String,
    val port: Int,
    val method: String,
    val fromAddress: String,
    val recipient: String,
    destinationType: String, // Smtp or Ses
) : BaseDestination(destinationType) {

    init {
        when (destinationType) {
            "smtp" -> {
                require(!Strings.isNullOrEmpty(host)) { "Host name should be provided" }
                require(port > 0) { "Port should be positive value" }
                validateEmail(fromAddress)
                validateEmail(recipient)
            }
            // TODO Add ses here
        }
    }
}
