// @file:JsFileName("Adapter.js")
@file:Suppress("unused")

import models.Book
import kotlin.js.JsExport.Ignore

@OptIn(ExperimentalJsExport::class)
@JsExport
object Adapter {

    @Ignore
    fun ignored(): String = "Should not be callable from JS"

    // cannot declare commonMain type instances, because they are not exported
    // to JS (aka marked as non-external)

    // needs to be Array type because List is not exported to JS
    // supported types at: https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
    fun getBooks(): Array<Book> = KmpApi.getBooks().toTypedArray()
    fun getPlatformType(): String = KmpApi.getPlatformType()

    @JsName("addBooks") // renames a method when exported to JS
    fun executePredefinedLogic() = KmpApi.executePredefinedLogic()
    fun clearBooks() = KmpApi.clearBooks()
}