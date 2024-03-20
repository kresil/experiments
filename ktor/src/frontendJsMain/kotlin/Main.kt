import config.wsClient
import connection.ChatConnHandler
import dom.DomHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val wsClientJs = wsClient
    GlobalScope.launch {
        ChatConnHandler.initConnection(
            this,
            wsClient = wsClientJs,
            writeMessage = DomHandler::appendMessage
        )
    }
    DomHandler.setupEventListeners(wsClientJs)
}