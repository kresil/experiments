package plugins

import application.models.ChatSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*

private const val NAME = "SESSION"

/**
 * Enables the use of sessions to keep information between requests/refreshes of the browser.
 */
fun Application.configureSessions() =
    install(Sessions) {
        cookie<ChatSession>(NAME)
    }