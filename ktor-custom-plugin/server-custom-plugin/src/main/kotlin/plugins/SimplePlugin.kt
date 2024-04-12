package plugins

import io.ktor.server.application.*

val SimplePlugin = createApplicationPlugin(name = "SimplePlugin") {
    println("SimplePlugin is installed!")
}
