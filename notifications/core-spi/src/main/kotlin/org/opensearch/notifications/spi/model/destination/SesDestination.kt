/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

import com.amazonaws.regions.Regions
import org.opensearch.core.common.Strings
import org.opensearch.notifications.spi.utils.validateEmail

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
