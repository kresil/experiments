package connection

import client.WsClient
import config.serverConfig
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
        appendMessage: suspend (message: String) -> Unit,
    ) {
        try {
            wsClient.connect()
            wsClient.receive {
                scope.launch { appendMessage(it) }
            }
        } catch (e: Throwable) {
            if (e is ClosedReceiveChannelException) {
                appendMessage("Disconnected. ${e.message}.")
            } else if (e is WebSocketException) {
                appendMessage("Unable to connect.")
            }
            scheduleReconnect(scope, 5000) {
                initConnection(scope, wsClient, appendMessage)
            }
        }
    }

    suspend fun testConnection(): String {
        val response = wsClient.client.get(serverConfig.host)
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