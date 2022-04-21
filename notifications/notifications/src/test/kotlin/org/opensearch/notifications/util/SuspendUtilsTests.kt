package org.opensearch.notifications.util

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.index.IndexResponse
import org.opensearch.client.Client
import org.opensearch.notifications.util.SuspendUtils.Companion.suspendUntilTimeout

@ExtendWith(MockitoExtension::class)
internal class SuspendUtilsTests {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var client: Client

    @Mock
    lateinit var mockedIndexRequest: IndexRequest

    @Test
    fun `test SuspendUntilTimeout TimeoutCancellationException`() {
        runBlocking {
            assertThrows<TimeoutCancellationException> {
                // setting low timeout to always timeout
                client.suspendUntilTimeout<Client, IndexResponse>(0) {
                    client.index(mockedIndexRequest)
                }
            }
        }
    }

    @Test
    fun `test SuspendUntilTimeout RuntimeException`() {
        runBlocking {
            whenever(client.index(mockedIndexRequest)).thenThrow(RuntimeException())
            assertThrows<RuntimeException> {
                client.suspendUntilTimeout<Client, IndexResponse>(1000) {
                    client.index(mockedIndexRequest)
                }
            }
        }
    }
}
