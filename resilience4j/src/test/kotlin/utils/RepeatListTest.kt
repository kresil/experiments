package utils

import kotlin.test.Test
import kotlin.test.assertEquals

class RepeatListTest {

    @Test
    fun `repeat a list of integers`() {
        val list = listOf(1, 2, 3)
        val repeatedList = list.repeat(3)
        assertEquals(listOf(1, 2, 3, 1, 2, 3, 1, 2, 3), repeatedList)
    }

    @Test
    fun `repeat a list of strings`() {
        val list = listOf("a", "b", "c")
        val repeatedList = list.repeat(2)
        assertEquals(listOf("a", "b", "c", "a", "b", "c"), repeatedList)
    }

}