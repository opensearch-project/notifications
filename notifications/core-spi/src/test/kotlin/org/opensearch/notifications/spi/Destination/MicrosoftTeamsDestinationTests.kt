/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.spi.Destination

import org.opensearch.notifications.spi.model.destination.DestinationType
import org.opensearch.notifications.spi.model.destination.MicrosoftTeamsDestination
class MicrosoftTeamsDestinationTests {
    fun testMicrosoftTeamsDestination() {
        val url = "https://8m7xqz.webhook.office.com/webhookb2/b0885113-57f8-4b61-8f3a-bdf3f4ae2831@500d1839-8666-4320-9f55-59d8838ad8db/IncomingWebhook/84637be48f4245c09b82e735b2cd9335/b7e1bf56-6634-422c-abe8-402e6e95fc68"
        val destination = MicrosoftTeamsDestination(url)
        assert(destination.url == url) { "URL assertion failed" }
        assert(destination.destinationType == DestinationType.MICROSOFT_TEAMS) { "Destination type assertion failed" }
        // Test sendMessage function
        val message = "Hello World"
        destination.sendMessage(message, url)
        assert(response.isSuccessful) { "Failed to post message to Microsoft Teams, response code: ${response.code()}" }
    }
}
