# Ktor Retry Plugin

### Install

The `HttpRequestRetry` plugin can be installed using the `install` function
and configured using its **last parameter function**
(_trailing lambda_), as with other Ktor plugins.

The plugin is part of the `ktor-client-core` module.

### Configuration

| Property             | Description                                                                                                                                                                        |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `maxRetries`         | The maximum number of retries                                                                                                                                                      |                                                                                                                                                     |
| `retryIf`            | A lambda that returns `true` if the request should be retried on specific request details and or responses.                                                                        |
| `retryOnExceptionIf` | A lambda that returns `true` if the request should be retried on specific request details and or exceptions that occurred.                                                         |
| `delayMillis`        | A lambda that returns the delay in milliseconds before the next retry. Some methods are provided to create more complex delays, such as `exponentialDelay()` or `constantDelay()`. |

> [!NOTE]
> The plugin also provides more specific methods to retry for
> (e.g., on server errors, for example, `retryOnServerErrors()` retries on server 5xx errors).

Example:

```kotlin
val client = HttpClient(CIO) {
    install(HttpRequestRetry) {
        maxRetries = 5
        retryIf { request, response ->
            !response.status.isSuccess()
        }
        retryOnExceptionIf { request, cause ->
            cause is NetworkError
        }
        delayMillis { retry ->
            retry * 3000L
        } // retries in 3, 6, 9, etc. seconds
    }
    // other configurations
}
```

Default configuration:

```kotlin
install(HttpRequestRetry) {
    retryOnExceptionOrServerErrors(3)
    exponentialDelay()
}
```

### Changing a Request Before Retry

It is possible to modify the request
before retrying it by using the `modifyRequest` method inside the configuration block of the plugin.
This method receives a lambda that takes the request and returns the modified request.
One usage example is to add a header with the current retry count:

```kotlin
val client = HttpClient(CIO) {
    install(HttpRequestRetry) {
        modifyRequest { request ->
            request.headers.append("x-retry-count", retryCount.toString())
        }
    }
    // other configurations
}
```

> [!IMPORTANT]
> To preserve configuration context between retry attempts, the plugin uses request attibutes to store data.
> If those are altered, the plugin may not work as expected.
> 
> If an attribute is not present in the request, the plugin will use the default configuration associated with that attribute.
> Such behaviour can be seen in the source code:
>   - [after applying configuration](https://github.com/ktorio/ktor/blob/7c76fa7c0f2b7dcc6e0445da8612d75bb5d11609/ktor-client/ktor-client-core/common/src/io/ktor/client/plugins/HttpRequestRetry.kt#L366-L373)
>   - [before each retry attempt](https://github.com/ktorio/ktor/blob/7c76fa7c0f2b7dcc6e0445da8612d75bb5d11609/ktor-client/ktor-client-core/common/src/io/ktor/client/plugins/HttpRequestRetry.kt#L267-L274)
