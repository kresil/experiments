import e2e.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import java.io.File

class ApplicationTest {

    companion object {
        private const val GRADLEW_WINDOWS = "gradlew.bat"
        private const val GRADLEW_UNIX = "gradlew"

        @JvmStatic
        fun findGradleWrapper(): String {
            val currentDir = File(System.getProperty("user.dir"))
            val parentDir = currentDir.parent ?: error("Cannot find parent directory of $currentDir")
            val gradlewName = if (System.getProperty("os.name").startsWith("Windows")) {
                GRADLEW_WINDOWS
            } else {
                GRADLEW_UNIX
            }
            val gradlewFile = File(parentDir, gradlewName)
            check(gradlewFile.exists())  { "Gradle Wrapper not found at ${gradlewFile.absolutePath}" }
            return gradlewFile.absolutePath
        }
    }

    @Before
    fun setup() {
        System.setProperty("gradlew", findGradleWrapper())
    }

    @Test
    fun requestContainsCustomHeader(): Unit = runBlocking {
        runGradleAppWaiting().inputStream.readString().let { outputString ->
            assertTrue(outputString.contains("X-Custom-Header: Hello, world!"))
            assertTrue(outputString.contains("Content-Type: text/html"))
            assertTrue(outputString.contains("Read response delay (ms)"))
        }
    }
}
