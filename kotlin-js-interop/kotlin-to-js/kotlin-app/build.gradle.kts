plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
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

    sourceSets {
        jsMain {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}
