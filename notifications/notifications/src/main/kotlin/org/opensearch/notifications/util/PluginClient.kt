/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.util

import org.opensearch.transport.client.Client

/** The notifications plugin's client that runs actions as its system subject. */
class PluginClient(delegate: Client) : org.opensearch.commons.client.PluginClient(delegate)
