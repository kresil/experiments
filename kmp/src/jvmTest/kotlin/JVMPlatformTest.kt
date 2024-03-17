import target.Platform
import target.PlatformType
import target.Target
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class JVMPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.JVM, target.getPlatform().type)
    }
}
