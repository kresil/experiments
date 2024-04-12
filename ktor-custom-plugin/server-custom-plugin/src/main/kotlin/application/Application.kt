package application

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import plugins.CustomHeaderPlugin
import plugins.DataTransformationBenchmarkPlugin
import plugins.DataTransformationPlugin
import plugins.RequestLoggingPlugin
import plugins.SimplePlugin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(SimplePlugin)
    install(RequestLoggingPlugin)
    install(DataTransformationPlugin)
    install(CustomHeaderPlugin) {
        headerName = "X-Custom-Header"
        headerValue = "Hello, world!"
    }
    install(DataTransformationBenchmarkPlugin)
    routing {
        get("/") {
            call.respondText("Root page")
        }
        get("/index") {
            call.respondText("Index page")
        }
        post("/transform-data") {
            val data = call.receive<Int>()
            call.respond(data)
        }
    }
}
