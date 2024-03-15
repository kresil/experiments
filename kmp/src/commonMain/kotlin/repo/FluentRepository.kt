package repo

interface FluentRepository<T> {
    fun add(item: T): FluentRepository<T>
    fun remove(item: T): FluentRepository<T>
    fun getAll(): List<T>
    fun clear(): FluentRepository<T>
}