@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TargetRandomStringGen actual constructor(private val length: Int) {
    actual fun generate(): TargetString =
        Target.JS to randomStringGenFromNpm(length)
}