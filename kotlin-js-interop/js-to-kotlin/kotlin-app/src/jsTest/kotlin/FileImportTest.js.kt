import kotlin.test.Test
import kotlin.test.assertEquals

class FileImportTest {

    @Test
    fun testGreet() {
        assertEquals("Hello, John!", greet("John"))
    }

    @Test
    fun testAdvocate() {
        assertEquals("Kotlin advocate nr: 42", advocate(42))
    }
}
