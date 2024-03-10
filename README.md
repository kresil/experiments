# Experiments

## KMP - Kotlin Multiplatform

Module: [`kmp`](./kmp)

### Testing the Architecture

In the [KMP template](https://github.com/Kotlin/multiplatform-library-template) provided by Kotlin,
the example with the `fibonacci` sequence was removed
and replaced by a class to practice the `expect/actual` pattern more explicitly.

This addition follows the same principles:

- test common functionality in [`commonTest`](./kmp/src/commonTest/kotlin);
- test platform-specific functionality in each platform's test source set (`<Platform>Test`)

### Intermediate Source Sets

[Intermediate Source Sets](https://kotlinlang.org/docs/multiplatform-discover-project.html#intermediate-source-sets)
enable sharing code
across more than one platform,
yet not encompassing all, as that would be the role of the common source set.
This allows for a more fine-grained control over the code sharing, maximizing code reuse.

| ![Intermediate Source Set](./docs/imgs/inter-source-set.png) |
|:------------------------------------------------------------:|
|               Intermediate Source Set Example                |

The dependencies between source sets can be configured within the corresponding `build.gradle.kts` file. Additionally,
their hierarchy can be adjusted,
as mentioned [here](https://kotlinlang.org/docs/multiplatform-hierarchy.html#manual-configuration).

> [!IMPORTANT]
> The `iOS` source set was removed from the template,
> because there is no `macOS` machine available for testing, as required by Apple.

### Relevant Design Choices

As mentioned in the issue [KT-61573](https://youtrack.jetbrains.com/issue/KT-61573), the `expect/actual` pattern
should only be used for `functions` and `interfaces`.
An alternative for this pattern is to use `expect fun` + `interface` in the common module.

| ![KT-61573](./docs/imgs/kt-61573.png) |
|:-------------------------------------:|
|               KT-61573                |

### Integration with JavaScript 

#### Javascript -> Kotlin

Example available [here](./kmp/src/jsMain/kotlin/randomstring.kt).

References:

- [JS to Kotlin Interop](https://kotlinlang.org/docs/js-to-kotlin-interop.html)
- [Dependencies from NPM](https://kotlinlang.org/docs/using-packages-from-npm.html)
- [Accessing External JavaScript Library](https://discuss.kotlinlang.org/t/kotlin-1-3-how-to-access-external-javascript-library-from-jsmain/15778)

## Ktor Framework

Module: [`ktor`](./ktor)

### Define Application Module

TODO()

### Installing Plugins

TODO()

### Defining Routes

TODO()

### Testing the Application

TODO()

### Client

TODO()
