/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.model.destination

/**
 * This class holds the contents of SNS destination
 */
data class SnsDestination(
    val topicArn: String,
    val roleArn: String? = null
) : BaseDestination(DestinationType.SNS) {
    // sample topic arn -> arn:aws:sns:us-west-2:075315751589:test-notification
    val region: String = topicArn.split(":".toRegex()).toTypedArray()[3]
}
