package repo

abstract class FluentRepositoryImpl<T> : FluentRepository<T> {

    protected abstract val container: MutableSet<T>

    override fun add(item: T): FluentRepositoryImpl<T> {
        container.add(item)
        return this
    }

    override fun remove(item: T): FluentRepositoryImpl<T> {
        container.remove(item)
        return this
    }

    override fun getAll(): List<T> = container.toList()

    override fun clear(): FluentRepositoryImpl<T> {
        container.clear()
        return this
    }

}