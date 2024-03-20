package config

import client.WsClient
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import models.HostPort

expect val serverConfig: HostPort

val wsClient = WsClient(HttpClient {
    install(WebSockets)
})