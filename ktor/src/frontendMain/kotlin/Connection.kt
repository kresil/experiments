import ChatDOMHandler.appendMessage
import io.ktor.client.plugins.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch

/**
 * Initializes the connection to the server and handles:
 * - the reconnection if the connection is lost within a timeout.
 * - the reception and sending of messages.
 */
@OptIn(DelicateCoroutinesApi::class)
suspend fun initConnection(wsClient: WsClient) {
    try {
        wsClient.connect()
        wsClient.receive(::appendMessage)
    } catch (e: Exception) {
        if (e is ClosedReceiveChannelException) {
            appendMessage("Disconnected. ${e.message}.")
        } else if (e is WebSocketException) {
            appendMessage("Unable to connect.")
        }

        window.setTimeout({
            GlobalScope.launch { initConnection(wsClient) }
        }, 5000)
    }
}