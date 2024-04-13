package application

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import plugins.CustomHeaderPlugin
import plugins.DataTransformationBenchmarkPlugin
import plugins.DataTransformationPlugin
import plugins.RequestLoggingPlugin
import plugins.SimplePlugin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf/yaml
fun Application.module() {
    println("Phases in this pipeline:")
    this.addPhase(PipelinePhase("CustomPhase"))
    this.items.forEach(::println)
    val sendPipeline: ApplicationSendPipeline = this.sendPipeline
    println("Send pipeline: ${sendPipeline.items}")
    val receivePipeline: ApplicationReceivePipeline = this.receivePipeline
    println("Receive pipeline: ${receivePipeline.items}"+ "\n")
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
