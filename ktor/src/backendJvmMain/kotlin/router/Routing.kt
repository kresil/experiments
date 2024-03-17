package router

import application.ChatServer
import application.WebSocketServerHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

/**
 * This function is an extension function for the [Application] class that allows to configure the routing for the
 * application.
 */
fun Application.configureRouting(server: ChatServer) {
    routing {
        // Defines a websocket `/ws` route that allows a protocol upgrade to convert a HTTP request/response request
        // into a bidirectional packetized connection.
        webSocket("/ws") {
            WebSocketServerHandler(server).apply { handleChatSession() }
        }
        get("/hello") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
        // This defines a block of static resources for the '/' path (since no path is specified and we start at '/')
        staticResources("/", "web")
    }
}