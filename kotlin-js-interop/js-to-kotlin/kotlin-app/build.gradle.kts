plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()
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
                implementation(npm("randomstring", "1.3.0"))
                implementation(npm("is-sorted", "1.0.5"))
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
