package repo

interface Repository<T> {
    val container: MutableSet<T>
    fun add(item: T): Repository<T>
    fun remove(item: T): Repository<T>
    fun getAll(): List<T>
    fun clear(): Repository<T>
}