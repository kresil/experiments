package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*

/**
 * Installs the Headers plugin to the application, which adds Date and Server headers to each response.
 */
fun Application.configureHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("Custom-Header", "some-value")
    }
}