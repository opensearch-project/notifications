package org.opensearch.notifications.spi.client

/**
 * This class provides Client to the relevant destinations
 */
internal object DestinationEmailClientPool {
    val emailClient: DestinationEmailClient = DestinationEmailClient()
}
