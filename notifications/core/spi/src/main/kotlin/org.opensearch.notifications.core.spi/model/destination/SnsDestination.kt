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

/**
 * This class holds the contents of SNS destination
 */
data class SnsDestination(
    val topicArn: String,
    val roleArn: String? = null,
) : BaseDestination(DestinationType.SNS) {
    // sample topic arn -> arn:aws:sns:us-west-2:075315751589:test-notification
    val region: String = topicArn.split(":".toRegex()).toTypedArray()[3]
}
