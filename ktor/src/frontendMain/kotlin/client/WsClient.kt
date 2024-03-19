package client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.HostPort

/**
 * A WebSocket client that connects to the server in the path `/ws` and sends and receives messages.
 * @property client The HTTP client.
 */
class WsClient(private val client: HttpClient, private val hostPort: HostPort) {
    private var session: WebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession(
            method = HttpMethod.Get,
            host = hostPort.host,
            port = hostPort.port,
            path = "/ws"
        )
    }

    suspend fun send(message: String) {
        session?.send(Frame.Text(message))
    }

    suspend fun receive(onReceive: (input: String) -> Unit) {
        withContext(Dispatchers.Default) {
            while (true) {
                val frame = session?.incoming?.receive()
                if (frame is Frame.Text) {
                    onReceive(frame.readText())
                }
            }
        }
    }
}