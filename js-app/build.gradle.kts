plugins {
    kotlin("js")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    // TODO: necessary to access adapter in jsMain?
    implementation(project(":kmp"))
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
