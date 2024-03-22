import kotlin.test.Test
import kotlin.test.assertEquals

class FuncImportTest {

    @Test
    fun testDefaultGreet() {
        assertEquals("Single default export: Hello, John!", defaultGreet("John"))
    }
}
