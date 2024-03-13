import target.Platform
import target.PlatformType
import target.Target
import kotlin.test.Test
import kotlin.test.assertEquals

class JVMPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.JVM, target.getPlatform().type)
    }
}
