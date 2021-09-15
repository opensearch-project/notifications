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

package org.opensearch.notifications.core.spi.model.destination

import com.amazonaws.regions.Regions
import org.opensearch.common.Strings
import org.opensearch.notifications.core.spi.utils.validateEmail

/**
 * This class holds the contents of ses destination
 */
class SesDestination(
    val accountName: String,
    val awsRegion: String,
    val roleArn: String?,
    val fromAddress: String,
    val recipient: String
) : BaseDestination(DestinationType.SES) {

    init {
        require(!Strings.isNullOrEmpty(awsRegion)) { "aws region should be provided" }
        require(Regions.values().any { it.getName() == awsRegion }) { "aws region is not valid" }
        validateEmail(fromAddress)
        validateEmail(recipient)
    }
}
