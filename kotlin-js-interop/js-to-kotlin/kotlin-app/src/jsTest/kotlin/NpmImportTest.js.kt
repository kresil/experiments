import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NpmImportTest {

    @Test
    fun testRandomStringFromNpm() {
        val randomString = RandomStringFromNpm.generate()
        assertTrue(randomString.length == 32)
    }

    @Test
    fun testRandomStringFromNpmWithLength() {
        val length = 3
        val randomString = RandomStringFromNpm.generate(length)
        assertEquals(randomString.length, length)
    }

    @Test
    fun testSorted() {
        val isArraySorted = sorted(arrayOf(1, 2, 3))
        assertTrue(isArraySorted)
    }

    @Test
    fun testSortedReversed() {
        val isArraySorted = sorted(arrayOf(3, 2, 1))
        assertFalse(isArraySorted)
    }
}
