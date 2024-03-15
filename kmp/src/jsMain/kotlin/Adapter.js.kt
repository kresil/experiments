// @file:JsFileName("Adapter.js")
@file:Suppress("unused")

import models.Book
import kotlin.js.JsExport.Ignore

@OptIn(ExperimentalJsExport::class)
@JsExport
object Adapter {

    @Ignore
    fun ignored(): String = "Should not be callable from JS"

    // val cant be used KmpApi as its non-external since it
    // was defined in commonMain

    // needs to be Array type because List is not exported to JS
    // supported types at: https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
    fun getBooks(): Array<Book> = KmpApi.getBooks().toTypedArray()
    fun getPlatformType(): String = KmpApi.getPlatformType()

    @JsName("addBooks")
    fun executePredefinedLogic() = KmpApi.executePredefinedLogic()
    fun clearBooks() = KmpApi.clearBooks()
}