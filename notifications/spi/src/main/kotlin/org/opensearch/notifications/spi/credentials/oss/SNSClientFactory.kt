package org.opensearch.notifications.spi.credentials.oss

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.opensearch.notifications.spi.credentials.SNSClient
import org.opensearch.notifications.spi.model.destination.SNSDestination

class SNSClientFactory : SNSClient {
    override fun getClient(destination: SNSDestination): AmazonSNS {
        val credentials = CredentialsProviderFactory().getCredentialsProvider(destination)
        return AmazonSNSClientBuilder.standard()
                .withRegion(destination.getRegion())
                .withCredentials(credentials)
                .build()
    }
}
