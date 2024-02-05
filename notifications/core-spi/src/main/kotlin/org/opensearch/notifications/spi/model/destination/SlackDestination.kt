/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.spi.model.destination

/**
 * This class holds the contents of a Slack destination
 */
class SlackDestination(
    url: String
) : WebhookDestination(url, DestinationType.SLACK)
