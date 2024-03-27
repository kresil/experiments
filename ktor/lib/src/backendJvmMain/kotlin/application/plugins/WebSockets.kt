package application.plugins

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import java.time.Duration

/**
 * This installs the WebSockets plugin to the application, which adds support for WebSockets.
 */
fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
    }
}