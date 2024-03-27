import application.ChatApplication
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * An entry point of the application.
 */
fun main() {
    embeddedServer(factory = Netty, port = 8080) {
        ChatApplication().apply { module() }
    }.start(wait = true)
}
