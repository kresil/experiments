import application.models.ChatSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*

/**
 * Defines interceptors to be installed in the application.
 */
fun Application.configureInterceptors() {
    // This adds an interceptor that will create a specific session in each
    // request if no session is available already.
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<ChatSession>() == null) {
            call.sessions.set(ChatSession(generateNonce()))
        }
    }
}