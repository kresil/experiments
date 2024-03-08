typealias TargetString = Pair<Target, String>

/**
 * Represents the target platform.
 */
enum class Target {
    JVM, NATIVE, ANDROID
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