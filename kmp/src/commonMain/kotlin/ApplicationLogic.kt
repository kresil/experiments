import models.Book
import repo.MemoryRepository
import repo.RepositoryImpl
import target.Platform
import target.Target

object ApplicationLogic {
    private val target = Target { Platform() }
    val repository = MemoryRepository(target)
    fun execute(): RepositoryImpl<Book> =
        repository
            .add(Book("The Hobbit", "J.R.R. Tolkien"))
            .add(Book("The Lord of the Rings", "J.R.R. Tolkien"))
            .remove(Book("The Little Prince", "Antoine de Saint-Exupéry"))
            .also {
                println("Items: ${repository.getAll()}")
                println("`Platform` Type: ${repository.getPlatformType()}")
            }
}