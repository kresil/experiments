package repo

import models.Book
import target.PlatformType
import target.Target

class MemoryRepository(private val target: Target) : RepositoryImpl<Book>() {

    override val container = mutableSetOf(
        Book("The Little Prince", "Antoine de Saint-Exupéry"),
        Book("Fire and Blood", "George R.R. Martin")
    )

    fun getPlatformType(): PlatformType = target.getPlatform().type
}