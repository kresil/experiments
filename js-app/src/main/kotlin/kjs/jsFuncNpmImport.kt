package kjs

@JsModule("randomstring")
@JsNonModule
external object RandomStringFromNpm {
    fun generate(
        // to test default parameter
        length: Int = definedExternally,
    ): String
}

@JsModule("is-sorted")
@JsNonModule
// to test generic function
external fun <T> sorted(a: Array<T>): Boolean