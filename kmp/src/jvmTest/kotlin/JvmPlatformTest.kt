import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import target.Platform
import target.PlatformType
import target.Target

class JvmPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.JVM, target.getPlatform().type)
    }
}
