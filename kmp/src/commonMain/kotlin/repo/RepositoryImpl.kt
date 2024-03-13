package repo

abstract class RepositoryImpl<T> : Repository<T> {

    override fun add(item: T): RepositoryImpl<T> {
        container.add(item)
        return this
    }

    override fun remove(item: T): RepositoryImpl<T> {
        container.remove(item)
        return this
    }

    override fun getAll(): List<T> = container.toList()

    override fun clear(): Repository<T> {
        container.clear()
        return this
    }

}