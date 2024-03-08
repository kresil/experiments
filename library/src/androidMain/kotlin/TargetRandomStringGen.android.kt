import java.util.Random

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TargetRandomStringGen actual constructor(private val length: Int) {
    actual fun generate(): TargetString =
        Target.ANDROID to ensureLength().toString()

    private fun ensureLength(): Int {
        var result: Int = 0
        while (true) {
            result += Random().nextInt()
            if (result.toString().length != length) {
                continue
            } else {
                break
            }
        }
        return result
    }
}