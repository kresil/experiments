# Ktor Framework

> [Ktor](https://ktor.io) is a modular KMP framework for developing asynchronous server and client systems in Kotlin.
>
> Developed by _JetBrains_, it was built with pure _Kotlin_ and is integrated with
> the [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
> system. This system allows asynchronous code to be defined sequentially
> and can be executed without blocking threads, taking greater advantage of the computing system
> available

## Table of Contents

- [Server](#server)
    - [Launching the Application](#launching-the-application)
    - [Application Module](#application-module)
    - [Installing Plugins](#installing-plugins)
    - [Defining Routes](#defining-routes)
    - [Testing the Application](#testing-the-application)
- [Client](#client)
    - [Requests](#requests)
    - [Responses](#responses)
- [Demonstration](#demonstration)
    - [Run Server](#run-server)
    - [Javascript Client](#javascript-client)
    - [Android Client](#android-client)

## Server

### Launching the Application

The application can be launched using the `Application` class.

```kotlin
fun main() {
    embeddedServer(Netty, port = 8000) {
        // ...
    }.start(wait = true)
}
```

### Application Module

In _Ktor_, the application module is defined using the `Application` class.

```kotlin
fun Application.module() {
    // ...
}
```

### Installing Plugins

A plugin can be installed using the `install` function and configured using its **last parameter function** (_trailing
lambda_).

```kotlin
fun Application.module() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
    }
    install(WebSockets) {
        // configuration if needed
    }
    // install(...)
}
```

To use specific plugins, they must be added to the dependencies in the correspondent `build.gradle.kts` file.

```kotlin
implementation("io.ktor:ktor-server-default-headers")
implementation("io.ktor:ktor-server-call-logging")
implementation("io.ktor:ktor-server-websockets")
```

### Defining Routes

Routes can be defined using the `routing` function.

```kotlin
fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json") {
            call.respond(mapOf("hello" to "world"))
        }
        // ...
    }
}
```

### Testing the Application

To test the application, you can utilize the `testApplication` function, which provides access to a `client` object for
making requests to the server.

Example:

```kotlin
testApplication {
    val log = arrayListOf<String>()
    // We perform a test websocket connection to this route. Effectively acting as a client.
    // The [incoming] parameter allows receiving frames, while the [outgoing] allows sending frames to the server.
    val client = client.config {
        install(WebSockets)
    }
    client.webSocket("/ws") {
        outgoing.send(Frame.Text("HELLO"))
        for (n in 0 until 2) {
            log += (incoming.receive() as Frame.Text).readText()
        }
    }
    assertEquals(listOf("Member joined", "HELLO"), log)
}
```

## Client

Similar to the `Application` class as seen in the [Server](#server) section,
the `HttpClient` class can be used to perform requests to a server and install plugins.

```kotlin
val client = HttpClient(CIO) {
    install(Logging)
    // install(...)
}
```

### Requests

The `client` object can be used to make requests to the server.

```kotlin
val response: HttpResponse = client.get("http://localhost:8080")
```

```kotlin
@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String)

val response: HttpResponse = client.post("http://localhost:8080/customer") {
    contentType(ContentType.Application.Json)
    setBody(Customer(3, "Jet", "Brains"))
}
```

More examples [at](https://ktor.io/docs/request.html).

### Responses

The `HttpResponse` object can be used to access the response's status code, headers, and body.

```kotlin
val httpResponse: HttpResponse = client.get("https://ktor.io/")
val stringBody: String = httpResponse.body()
```

More examples [at](https://ktor.io/docs/response.html).

## Demonstration

### Run Server

To start the server, run the following command:

```bash
# from root
./gradlew ktor:backendJvmRun -DmainClass=MainKt --quiet
```

Code at [Main.kt](lib/src/backendJvmMain/kotlin/Main.kt).
Static files at [resources](lib/src/backendJvmMain/resources/web).

### Javascript Client

1. In any browser, access `http://localhost:8080`
2. Interact with the UI.

Code at [frontendJs](lib/src/frontendJsMain/kotlin).

### Android Client

1. Use [ngrok](https://ngrok.com/) to expose the server, which is running on localhost, to the internet, and in its terminal run:
    ```bash
    ngrok http http://localhost:8080
    ```
2. Grab the `url` string and replace the `host` parameter in
the [android-config](lib/src/frontendAndroidMain/kotlin/config/Config.android.kt) file.
3. Run the application on an emulator or physical device.

Code at [android-app](apps/android-app/src/main/java/android).
