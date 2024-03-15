@file:JsModule("../../../../../kotlin-js-interop/src/main/js/file-export.mjs")

package kjs

// defined in external javascript file
external fun greet(name: String): String
external fun advocate(nr: Int): String