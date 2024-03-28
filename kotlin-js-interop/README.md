# Kotlin-Js Interop

> The Kotlin-Js Interop is a feature of Kotlin/JS that allows the use of JavaScript libraries in Kotlin code and vice
> versa.

## Table of Contents

- [Javascript to Kotlin](#javascript-to-kotlin)
    - [Demonstrations](#demonstrations)
    - [NPM Dependencies](#npm-dependencies)
    - [Build and Run](#build-and-run)
- [Kotlin to Javascript](#kotlin-to-javascript)
    - [Demonstration](#demonstration)
    - [Build and Run](#build-and-run)
- [References](#references)

## Javascript to Kotlin

Module: [js-to-kotlin](js-to-kotlin)

### Demonstrations

<table>
<tr>
<td> <strong> Javascript </strong> </td> <td> <strong> Kotlin </strong> </td>
</tr>
<tr>
<td>

[src](js-to-kotlin/kotlin-app/src/jsMain)

</td>
<td>

[jsMain](js-to-kotlin/kotlin-app/src/jsMain)
<br>
[jsTest](js-to-kotlin/kotlin-app/src/jsTest)

</td>
</tr>
</table>

### NPM Dependencies

To use [npm](https://www.npmjs.com/) dependencies in Kotlin/JS,
the dependencies must be added to the `dependencies` block of the `build.gradle.kts` file.

```kotlin
dependencies {
    // Install npm dependencies
    implementation(npm("randomstring", "1.3.0"))
}
```

And then define the exported function according to the JavaScript library's API.

<table>
<tr>
<td> <strong> Javascript </strong> </td> <td> <strong> Kotlin </strong> </td>
</tr>
<tr>
<td>

```javascript
var randomstring = require("randomstring");

randomstring.generate();
// >> "XwPp9xazJ0ku5CZnlmgAx2Dld8SHkAeT"

randomstring.generate(7);
// >> "xqm5wXX"
```

</td>
<td>

```kotlin
@JsModule("randomstring")
@JsNonModule
external object RandomStringFromNpm {
    fun generate(
        length: Int = definedExternally,
    ): String
}
```

</td>
</table>

> [!TIP]
> To delegate default parameter value to the imported JavaScript function, use `definedExternally`.

### Build and Run

```bash
# from root
cd js-to-kotlin/kotlin-app
./gradlew cleanAllTests allTests --rerun-tasks
```

## Kotlin to Javascript

Module: [kotlin-to-js](kotlin-to-js)

### Demonstration

<table>
<tr>
<td> <strong> Kotlin </strong> </td> <td> <strong> Javascript </strong>  </td>
</tr>
<tr>
<td>

[src](kotlin-to-js/kotlin-app/src/jsMain/kotlin)

</td>
<td>

[js-test](kotlin-to-js/js-app/src)

</td>
</table>

### Build and Run

1. Execute the perl [script](kotlin-to-js/js-app/npm-link-script.pl) to link local npm packages in this project.
2. Run the tests:
    ```bash
    # from root
    cd kotlin-to-js/js-app
    npm test
    ```

## References

- [Kotlinlang: JS to Kotlin Interop](https://kotlinlang.org/docs/js-to-kotlin-interop.html)
- [Kotlinlang: Kotlin to JS Interop](https://kotlinlang.org/docs/js-interop.html)
- [Kotlinlang: Dependencies from NPM](https://kotlinlang.org/docs/using-packages-from-npm.html)
- [Kt.Academy: JS Interop](https://kt.academy/article/ak-js-interop)
- [Dev.to: @JsExport guide for exposing Kotlin to JS](https://dev.to/touchlab/jsexport-guide-for-exposing-kotlin-to-js-20l9)
