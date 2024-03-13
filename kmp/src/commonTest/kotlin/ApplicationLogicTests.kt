import models.Book
import repo.MemoryRepository
import target.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationLogicTests {

    @Test
    fun testContainerBeforeApplicationLogic() {
        val repository = MemoryRepository { Platform() }
        assertEquals(
            listOf(
                Book("The Little Prince", "Antoine de Saint-Exupéry"),
                Book("Fire and Blood", "George R.R. Martin")
            ),
            repository.getAll()
        )
    }

    @Test
    fun testApplicationLogicExecuteMethod() {
        val logic = ApplicationLogic
        val repository = logic.execute()
        assertEquals(
            setOf(
                Book("Fire and Blood", "George R.R. Martin"),
                Book("The Hobbit", "J.R.R. Tolkien"),
                Book("The Lord of the Rings", "J.R.R. Tolkien")
            ),
            repository.getAll().toSet()
        )
    }

    @Test
    fun testClearAllBooks() {
        val logic = ApplicationLogic
        logic.repository.clear()
        assertEquals(emptyList(), logic.repository.getAll())
    }
}
