package target

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Platform actual constructor() {
    actual val type: PlatformType
        get() = PlatformType.Native
}