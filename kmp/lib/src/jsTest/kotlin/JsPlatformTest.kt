import target.Platform
import target.PlatformType
import target.Target
import kotlin.test.Test
import kotlin.test.assertEquals

class JsPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.JS, target.getPlatform().type)
    }
}
