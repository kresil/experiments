import client.WsClient
import connection.initConnection
import dom.DomHandler
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.HostPort

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val hostPort = HostPort(
        host = window.location.hostname,
        port = window.location.port.toInt(),
    )
    val wsClient = WsClient(HttpClient { install(WebSockets) }, hostPort)
    GlobalScope.launch {
        initConnection(
            this,
            wsClient = wsClient,
            writeMessage = DomHandler::appendMessage
        )
    }
    DomHandler.setupEventListeners(wsClient)
}