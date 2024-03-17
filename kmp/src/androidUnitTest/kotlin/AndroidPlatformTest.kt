import target.Platform
import target.PlatformType
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.Android, target.getPlatform().type)
    }
}
