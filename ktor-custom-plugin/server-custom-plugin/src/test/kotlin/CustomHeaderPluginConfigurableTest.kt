import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import plugins.CustomHeaderPluginConfigurable
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomHeaderPluginConfigurableTest {
    @Test
    fun testRoot() = testApplication {
        application {
            install(CustomHeaderPluginConfigurable)
        }
        client.get("/").apply {
            assertEquals("Some value", headers["X-Another-Custom-Header"].toString())
        }
    }
}
