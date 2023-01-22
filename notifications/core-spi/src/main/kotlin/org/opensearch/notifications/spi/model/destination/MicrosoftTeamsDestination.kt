package org.opensearch.notifications.spi.model.destination
import org.opensearch.notifications.spi.utils.validateUrl
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
class MicrosoftTeamsDestination(url: String) : WebhookDestination(url, DestinationType.MICROSOFT_TEAMS) {
    init {
        validateUrl(url)
    }

    fun sendMessage(message: String, url: String) {
        val connectionUrl = URL(url)
        val connection = connectionUrl.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val outputStream = connection.outputStream
        val writer = OutputStreamWriter(outputStream)
        writer.write("{\"text\":\"$message\"}")
        writer.flush()
        writer.close()
        outputStream.close()

        connection.responseCode
    }
}
