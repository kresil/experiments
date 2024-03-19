/*
import client.WsClient
import connection.initConnection
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import models.HostPort

fun main() {
    val hostPort = HostPort("localhost", 8080, "/ws")
    val wsClient = WsClient(HttpClient { install(WebSockets) }, hostPort)
    runBlocking {
        initConnection(
            this,
            wsClient = wsClient,
            writeMessage = writeMessage
        )
    }
}*/
