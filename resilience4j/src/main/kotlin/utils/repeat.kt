package utils

/**
 * Repeats a list content n times.
 * ```kotlin
 * val list = listOf(1, 2, 3)
 * val repeatedList = list.repeat(3)
 * // repeatedList is [1, 2, 3, 1, 2, 3, 1, 2, 3]
 * ```
 * @param n the number of times to repeat the list
 */
fun <T> List<T>.repeat(n: Int): List<T> {
    return List(n) { this }.flatten()
}