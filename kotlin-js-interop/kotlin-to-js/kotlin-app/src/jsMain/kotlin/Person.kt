@file:Suppress("unused")

import kotlin.js.json

@OptIn(ExperimentalJsExport::class)
@JsExport
class Person(private val name: String) {

    fun hello() : String = "Hello $name!"

    @JsName("helloWithGreeting")
    fun hello(greeting: String) = "$greeting $name!"

    fun useConsole() {
        console.log("Hello $name!")
    }

    fun toJson(): String {
        val o = json(
            "name" to "John",
            "age" to 42,
        )
        // {"name":"John","age":42}
        return JSON.stringify(o)
    }

    fun accessJsonProps(): dynamic {
        /**
         * The result of js is dynamic, which is a special type for Kotlin/JS
         * that is not checked by the compiler and represents any JavaScript value.
         * This means we can assign it to any variable, call any function on it,
         * and cast it to any type.
         */
        val o: dynamic = js("{name: 'John', surname: 'Foo'}")
        return o
    }

}