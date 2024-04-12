package application

import application.plugins.configureHeaders
import application.plugins.configureMonitoring
import application.plugins.configureSessions
import application.plugins.configureStatusPages
import application.plugins.configureWebSockets
import application.router.configureRouting
import application.server.ChatServer
import configureInterceptors
import io.ktor.server.application.*

/**
 * This is the main class of the application.
 */
class ChatApplication {

    // Note: Cannot be object otherwise tests will accumulate state
    private val server = ChatServer()

    /**
     * Defines the application main module with:
     * - necessary plugins to be installed
     * - routing configuration
     */
    fun Application.module() {

        configureHeaders()
        configureMonitoring()
        configureSessions()
        configureWebSockets()
        configureRouting(server)
        configureInterceptors()
        configureStatusPages()
    }
}
