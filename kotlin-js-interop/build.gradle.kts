plugins {
    kotlin("js")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    // Install npm dependencies
    implementation(npm("randomstring", "1.3.0"))
    implementation(npm("is-sorted", "1.0.5"))
}

kotlin {
    js(compiler = IR) {
        // Explicitly instructs the Kotlin compiler to emit executable .js files,
        // required when using IR compiler
        binaries.executable()
        nodejs {

        }
        // useCommonJs()
        useEsModules()
    }
}
