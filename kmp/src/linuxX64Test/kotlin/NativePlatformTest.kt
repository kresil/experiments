import target.Platform
import target.PlatformType
import target.Target
import kotlin.test.Test
import kotlin.test.assertEquals

class NativePlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.Native, target.getPlatform().type)
    }
}
