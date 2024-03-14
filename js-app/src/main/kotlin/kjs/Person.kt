package kjs

import kotlin.js.json

@OptIn(ExperimentalJsExport::class)
@JsExport
class Person(private val name: String) {

    fun hello() {
        println("Hello $name!")
    }

    @JsName("helloWithGreeting")
    fun hello(greeting: String) {
        println("$greeting $name!")
    }

    fun useConsole() {
        console.log("Hello $name!")
    }

    fun toJson() {
        val o = json(
            "name" to "John",
            "age" to 42,
        )
        // {"name":"John","age":42}
        println(JSON.stringify(o))
    }

    fun accessJsonProps() {
        /**
         * The result of js is dynamic, which is a special type for Kotlin/JS
         * that is not checked by the compiler and represents any JavaScript value.
         * This means we can assign it to any variable, call any function on it,
         * and cast it to any type.
         */
        val o: dynamic = js("{name: 'John', surname: 'Foo'}")
        println(o.name) // John
        println(o.surname) // Foo
        println(o.toLocaleString()) // [object Object]
        println(o.unknown) // undefined
    }

}