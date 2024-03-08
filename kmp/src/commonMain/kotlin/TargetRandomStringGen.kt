typealias TargetString = Pair<Target, String>

/**
 * Represents a target platform.
 */
enum class Target {
    JVM, NATIVE, ANDROID, JS
}

/**
 * Generates a random string.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TargetRandomStringGen(length: Int) {
    /**
     * Generates a random string with the given length.
     */
    fun generate(): TargetString
}