// reminder: this is an intermidiate source set file, which means it is not part of the commonMain,
// but it is part of the common source set of the native target, which in this case can
// include: ios, linux, macos, mingw, tvos, watchos
// more at: https://kotlinlang.org/docs/multiplatform-discover-project.html#intermediate-source-sets
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TargetRandomStringGen actual constructor(private val length: Int) {
    actual fun generate(): TargetString =
        Target.ANDROID to "platform specific string".take(length)
}

