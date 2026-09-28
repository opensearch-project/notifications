package org.opensearch.notifications.spi

/**
 * Thread-local holder for the application ID during notification send operations.
 * Set by the plugin layer before sending, read by the credential factories.
 */
object NotificationRequestContext {
    private val applicationId = ThreadLocal<String?>()

    fun setApplicationId(id: String?) {
        applicationId.set(id)
    }

    fun getApplicationId(): String? = applicationId.get()

    fun clear() {
        applicationId.remove()
    }

    fun threadLocal(): ThreadLocal<String?> = applicationId
}
