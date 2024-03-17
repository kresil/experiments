import org.junit.jupiter.api.Assertions.assertEquals
import target.Platform
import target.PlatformType
import target.Target
import org.junit.jupiter.api.Test

class AndroidPlatformTest {

    @Test
    fun testTargetCorrespondsToCurrentPlatform() {
        val target = Target { Platform() }
        assertEquals(PlatformType.Android, target.getPlatform().type)
    }
}
