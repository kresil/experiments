import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import target.Platform
import target.PlatformType
import target.Target

class AndroidPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.Android, target.getPlatform().type)
    }
}
