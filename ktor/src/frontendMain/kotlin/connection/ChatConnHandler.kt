package connection

import client.WsClient
import config.wsClient
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ChatConnHandler {
    /**
     * Initializes the connection to the server and handles:
     * - the reception and sending of messages.
     * - the reconnection to the server in case of disconnection, but only once.
     */
    suspend fun initConnection(
        scope: CoroutineScope,
        wsClient: WsClient,
        writeMessage: suspend (message: String) -> Unit,
    ) {
        try {
            wsClient.connect()
            wsClient.receive {
                scope.launch { writeMessage(it) }
            }
        } catch (e: Throwable) {
            if (e is ClosedReceiveChannelException) {
                writeMessage("Disconnected. ${e.message}.")
            } else if (e is WebSocketException) {
                writeMessage("Unable to connect.")
            }
            scheduleReconnect(scope, 5000) {
                initConnection(scope, wsClient, writeMessage)
            }
        }
    }

    suspend fun testConnection(): String {
        val response = wsClient.client.get("https://a12d-2001-8a0-6c7c-4800-d01a-22d1-33c7-91a3.ngrok-free.app")
        return response.bodyAsText()
    }

    private fun scheduleReconnect(
        scope: CoroutineScope,
        delayMillis: Long,
        block: suspend () -> Unit,
    ) {
        scope.launch {
            delay(delayMillis)
            block()
        }
    }
}