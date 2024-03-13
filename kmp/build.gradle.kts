plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("module.publication")
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
    js {
        useCommonJs()
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
        /*val nativeMain by getting {
            dependsOn(commonMain)
        }

        val nativeTest by getting {
            dependsOn(commonTest)
        }*/

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
            dependsOn(commonMain)
        }

        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

android {
    namespace = "kresil.experiments"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}


// TODO: jvmTest, testDebugUnitTest, testReleaseUnitTest add to allTests task