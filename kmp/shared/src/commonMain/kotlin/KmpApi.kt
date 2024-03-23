import models.Book
import repo.MemoryFluentRepository
import target.Platform
import target.Target

object KmpApi {
    private val target = Target { Platform() }
    val repository = MemoryFluentRepository(target)
    fun clearBooks() {
        repository.clear()
    }

    fun getBooks(): List<Book> = repository.getAll()
    fun getPlatformType(): String = "${repository.getPlatformType()}"
    fun executePredefinedLogic() {
        repository
            .add(Book("The Hobbit", "J.R.R. Tolkien"))
            .add(Book("The Lord of the Rings", "J.R.R. Tolkien"))
            .remove(Book("The Little Prince", "Antoine de Saint-Exupéry"))
    }
}