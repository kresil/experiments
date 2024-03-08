import kotlin.test.Test
import kotlin.test.assertEquals

class NativeTargetRandomStringGenTest {

    @Test
    fun `test target object`() {
        val length = 10
        val (target, _) = TargetRandomStringGen(length).generate()
        assertEquals(Target.NATIVE, target)
    }
}
