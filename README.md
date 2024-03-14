# Experiments

## KMP - Kotlin Multiplatform

Module: [kmp](./kmp)

### Testing the Architecture

In the [KMP template](https://github.com/Kotlin/multiplatform-library-template) provided by Kotlin,
the example with the `fibonacci` sequence was removed
and replaced by a class to practice the `expect/actual` pattern more explicitly.

This addition follows the same principles:

- test common functionality in [`commonTest`](./kmp/src/commonTest/kotlin);
- test platform-specific functionality in each platform's test source set (`<target.`Platform.jvm`>Test`)

> To run the tests for all supported targets, use the command `./gradlew kmp:allTests`.

### Intermediate Source Sets

[Intermediate Source Sets](https://kotlinlang.org/docs/multiplatform-discover-project.html#intermediate-source-sets)
enable sharing code
across more than one platform,
yet not encompassing all, as that would be the role of the common source set.
This allows for a more fine-grained control over the code sharing, maximizing code reuse.

| ![Intermediate Source Set](./docs/imgs/inter-source-set.png) |
|:------------------------------------------------------------:|
|               Intermediate Source Set Example                |

The dependencies between source sets can be configured within the corresponding `build.gradle.kts` file. Additionally,
their hierarchy can be adjusted,
as mentioned [here](https://kotlinlang.org/docs/multiplatform-hierarchy.html#manual-configuration).

> [!IMPORTANT]
> The `iOS` source set was removed from the template,
> because there is no `macOS` machine available for testing, as required by Apple.

### Relevant Design Choices

As mentioned in the issue [KT-61573](https://youtrack.jetbrains.com/issue/KT-61573), the `expect/actual` pattern
should only be used for `functions` and `interfaces`.
An alternative for this pattern is to use `expect fun` + `interface` in the common module.

| ![KT-61573](./docs/imgs/kt-61573.png) |
|:-------------------------------------:|
|               KT-61573                |

### Integration with JavaScript

References:

- [JS to Kotlin Interop](https://kotlinlang.org/docs/js-to-kotlin-interop.html)
- [Dependencies from NPM](https://kotlinlang.org/docs/using-packages-from-npm.html)
- [Accessing External JavaScript Library](https://discuss.kotlinlang.org/t/kotlin-1-3-how-to-access-external-javascript-library-from-jsmain/15778)

## Ktor Framework

Module: [ktor](./ktor)

### Launching the Application

The application can be launched using the `Application` class.

```kotlin
fun main() {
    embeddedServer(Netty, port = 8000) {
        // ...
    }.start(wait = true)
}
```

### Define Application Module

In Ktor, the application module is using the `Application` class.

```kotlin
fun Application.module() {
    // ...
}
```

### Installing Plugins

Each plugin has its own configuration, which can be set using the `install` function.

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

To test the application, the `testApplication` function can be used which exposes a `client` object that
can be used to perform requests to the server.

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
        // Send a HELLO message
        outgoing.send(Frame.Text("HELLO"))

        // We then receive two messages (the message notifying that the member joined, and the message we sent echoed to us)
        for (n in 0 until 2) {
            log += (incoming.receive() as Frame.Text).readText()
        }
    }

    // asserts
    assertEquals(listOf("Member joined", "HELLO"), log)
}
```

### Client

Similar to the `Application` class,
the `HttpClient` class can be used to perform requests to a server and install plugins.

```kotlin
val client = HttpClient(CIO) {
    install(Logging)
    // install(...)
}
```

#### Requests

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

More [at](https://ktor.io/docs/request.html).

#### Responses

The `HttpResponse` object can be used to access the response's status code, headers, and body.

```kotlin
val httpResponse: HttpResponse = client.get("https://ktor.io/")
val stringBody: String = httpResponse.body()
```

More [at](https://ktor.io/docs/response.html#body).
