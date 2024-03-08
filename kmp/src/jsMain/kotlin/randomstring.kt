// According to: https://www.npmjs.com/package/randomstring
// ref: https://kotlinlang.org/docs/using-packages-from-npm.html
@JsModule("randomstring")
external fun randomStringGenFromNpm(
    length: Int = definedExternally,
    readable: Boolean = definedExternally,
    charset: String = definedExternally,
    capitalization: String = definedExternally,
): String