import models.Book
import repo.MemoryFluentRepository
import target.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationLogicTests {

    @Test
    fun testRepoBeforeApplicationLogic() {
        val repository = MemoryFluentRepository { Platform() }
        assertEquals(
            listOf(
                Book("The Little Prince", "Antoine de Saint-Exupéry"),
                Book("Fire and Blood", "George R.R. Martin")
            ),
            repository.getAll()
        )
    }

    @Test
    fun testAfterApplicationLogic() {
        val logic = KmpApi
        logic.executePredefinedLogic()
        assertEquals(
            setOf(
                Book("Fire and Blood", "George R.R. Martin"),
                Book("The Hobbit", "J.R.R. Tolkien"),
                Book("The Lord of the Rings", "J.R.R. Tolkien")
            ),
            KmpApi.repository.getAll().toSet()
        )
    }

    @Test
    fun testClearAllBooks() {
        val logic = KmpApi
        logic.clearBooks()
        assertEquals(emptyList(), logic.getBooks())
    }

}
