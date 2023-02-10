internal class MicrosofTeamsDestinationTests {
    companion object {
        @JvmStatic
        fun escapeSequenceToRaw(): Stream<Arguments> =
            Stream.of(
                Arguments.of("\n", """\n"""),
                Arguments.of("\t", """\t"""),
                Arguments.of("\b", """\b"""),
                Arguments.of("\r", """\r"""),
                Arguments.of("\"", """\""""),
            )
    }

    @BeforeEach
    fun setup() {
        // Stubbing isHostInDenylist() so it doesn't attempt to resolve hosts that don't exist in the unit tests
        mockkStatic("org.opensearch.notifications.spi.utils.ValidationHelpersKt")
        every { org.opensearch.notifications.spi.utils.isHostInDenylist(any(), any()) } returns false
    }

    @Test
    fun `test teams message null entity response`() {
        val mockHttpClient = mockk<CloseableHttpClient>()

        // The DestinationHttpClient replaces a null entity with "{}".
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "{}")
        // TODO replace EasyMock in all UTs with mockk which fits Kotlin better
        val httpResponse = mockk<CloseableHttpResponse>()
        every { mockHttpClient.execute(any<HttpPost>()) } returns httpResponse

        every { httpResponse.code } returns RestStatus.OK.status
        every { httpResponse.entity } returns null

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.TEAMS to webhookDestinationTransport)

        val title = "test Teams"
        val messageText = "Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member call out: " +
            "@All All Present member call out: @Present"
        val url = "https://8m7xqz.webhook.office.com/webhookb2/b0885113-57f8-4b61-8f3a-bdf3f4ae2831@500d1839-8666-4320-9f55-59d8838ad8db/IncomingWebhook/84637be48f4245c09b82e735b2cd9335/b7e1bf56-6634-422c-abe8-402e6e95fc68"

        val destination = MicrosoftTeamsDestination(url)
        val message = MessageContent(title, messageText)

        val actualTeamsResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedWebhookResponse.statusText, actualTeamsResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualTeamsResponse.statusCode)
    }

    @Test
    fun `test teams message empty entity response`() {
        val mockHttpClient: CloseableHttpClient = EasyMock.createMock(CloseableHttpClient::class.java)
        val expectedWebhookResponse = DestinationMessageResponse(RestStatus.OK.status, "")

        // TODO replace EasyMock in all UTs with mockk which fits Kotlin better
        val httpResponse = mockk<CloseableHttpResponse>()
        every { mockHttpClient.execute(any<HttpPost>()) } returns httpResponse

        every { httpResponse.code } returns RestStatus.OK.status
        every { httpResponse.entity } returns StringEntity(responseContent)
        EasyMock.replay(mockHttpClient)

        val httpClient = DestinationHttpClient(mockHttpClient)
        val webhookDestinationTransport = WebhookDestinationTransport(httpClient)
        DestinationTransportProvider.destinationTransportMap = mapOf(DestinationType.CHIME to webhookDestinationTransport)

        val title = "test microsoft Teams "
        val messageText = "{\"Content\":\"Message gughjhjlkh Body emoji test: :) :+1: " +
            "link test: http://sample.com email test: marymajor@example.com All member call out: " +
            "@All All Present member call out: @Present\"}"
        val url = "https://8m7xqz.webhook.office.com/webhookb2/b0885113-57f8-4b61-8f3a-bdf3f4ae2831@500d1839-8666-4320-9f55-59d8838ad8db/IncomingWebhook/84637be48f4245c09b82e735b2cd9335/b7e1bf56-6634-422c-abe8-402e6e95fc68"

        val destination = MicrosoftTeamsDestination(url)
        val message = MessageContent(title, messageText)

        val actualChimeResponse: DestinationMessageResponse = NotificationCoreImpl.sendMessage(destination, message, "referenceId")

        assertEquals(expectedWebhookResponse.statusText, actualChimeResponse.statusText)
        assertEquals(expectedWebhookResponse.statusCode, actualChimeResponse.statusCode)
    }
}
