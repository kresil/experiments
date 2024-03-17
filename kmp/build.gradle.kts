plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("module.publication")
    kotlin("plugin.serialization") version "1.9.22"
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js(compiler = IR) {
        binaries.executable()
        useEsModules()
        browser {

        }
        nodejs {
        }
    }
    /*iosX64()
    iosArm64()
    iosSimulatorArm64()*/
    linuxX64()

    sourceSets {
        /**
         * - common
         *    - jvm
         *    - android
         *    - native
         *      - linuxX64
         *    - js
         *      - node
         *      - browser
         */

        // Source Set Category: Common
        // use `by creating` if a source set does not exist yet
        val commonMain by getting {
            dependencies {
                // put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // Source Set Category: Intermediary
        val nativeMain by getting {
            dependsOn(commonMain)
        }

        val nativeTest by getting {
            dependsOn(commonTest)
        }

        // Source Set Category: Platform
        val androidMain by getting {
            dependsOn(commonMain)
        }

        val androidUnitTest by getting {
            dependsOn(commonTest)
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                // implementation(npm("randomstring", "1.3.0"))
            }
        }
        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }

        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

android {
    namespace = "kresil.experiments"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    /*sourceSets.all {
        java.srcDirs(file("src/android${name.capitalize()}/kotlin"))
        res.srcDirs(file("src/android${name.capitalize()}/res"))
        resources.srcDirs(file("src/android${name.capitalize()}/resources"))
        manifest.srcFile(file("src/android${name.capitalize()}/AndroidManifest.xml"))
    }*/
    sourceSets {
        getByName("main") {
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }
        getByName("test") {
            java.srcDirs("src/androidTest/kotlin")
            res.srcDirs("src/androidTest/res")
        }
    }
}
