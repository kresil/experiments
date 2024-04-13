package application

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import plugins.CustomHeaderConfigurablePlugin
import plugins.LoggingHeadersPlugin
import plugins.ResponseTimePlugin

fun main() {
    runBlocking {
        val client = HttpClient(CIO) {
            install(CustomHeaderConfigurablePlugin) {
                headerName = "X-Custom-Header"
                headerValue = "Hello, world!"
            }
            install(LoggingHeadersPlugin)
            install(ResponseTimePlugin)
        }
        // in order to understand the pipeline,
        val requestPipeline: HttpRequestPipeline = client.requestPipeline
        val customPhase = PipelinePhase("CustomPhase")
        requestPipeline.addPhase(customPhase)
        requestPipeline.intercept(HttpRequestPipeline.Before) {
            println("Intercepting before phase in request pipeline")
        }
        requestPipeline.intercept(customPhase) {
            println("Intercepting custom phase in request pipeline")
        }
        logPipeline("Request pipeline: ", requestPipeline)
        logPipeline("Send pipeline: ", client.sendPipeline)
        logPipeline("Response pipeline: ", client.responsePipeline)
        logPipeline("Receive pipeline: ", client.receivePipeline)
        client.get("https://ktor.io/")
    }
}

private fun logPipeline(name: String, pipeline: Pipeline<*, *>) {
    val requestPipelinePhases = pipeline.items
    println(name)
    requestPipelinePhases.forEach {
        println("- $it")
        println("- Interceptors: ${pipeline.interceptorsForPhase(it)}")
        println()
    }
}
