package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * This installs the Monitoring plugin to the application, which adds logging information to requests.
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        // This uses the logger to log calls (request/response)
        filter { call -> call.request.path().startsWith("/") }
    }
}

