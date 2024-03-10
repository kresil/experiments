import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val wsClient = WsClient(HttpClient { install(WebSockets) })
    GlobalScope.launch { initConnection(wsClient) }
    ChatDOMHandler.setupEventListeners(wsClient)
}
