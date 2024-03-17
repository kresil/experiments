package application

import configureInterceptors
import io.ktor.server.application.*
import plugins.configureHeaders
import plugins.configureMonitoring
import plugins.configureSessions
import plugins.configureStatusPages
import plugins.configureWebSockets
import router.configureRouting

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
