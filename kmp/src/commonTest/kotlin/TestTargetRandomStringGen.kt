import kotlin.test.Test
import kotlin.test.assertEquals

class TestTargetRandomStringGen {

    @Test
    fun `test random string generation`() {
        val length = 10
        val (_, randomString) = TargetRandomStringGen(length).generate()
        assertEquals(length, randomString.length)
    }
}
