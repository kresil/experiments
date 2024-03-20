import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("backendJvm") // this creates both Main and Test source sets
    androidTarget("frontendAndroid") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js("frontendJs", IR) {
        browser {
            testTask { enabled = false }
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory = file("$projectDir/src/backendJvmMain/resources/web")
            }
            binaries.executable()
        }
    }

    sourceSets.forEach {
        it.dependencies {
            implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:2.3.9"))
        }
    }

    sourceSets {

        val commonMain by getting {
        }

        val commonTest by getting {
        }

        val backendJvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-websockets")
                implementation("io.ktor:ktor-server-call-logging")
                implementation("io.ktor:ktor-server-default-headers")
                implementation("io.ktor:ktor-server-sessions")
                implementation("io.ktor:ktor-server-status-pages")
                implementation("ch.qos.logback:logback-classic:1.4.6")
            }
        }

        val backendJvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation("io.ktor:ktor-server-test-host")
                implementation("io.ktor:ktor-client-websockets")
            }
        }

        val frontendMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation("io.ktor:ktor-client-websockets")
            }
        }

        val frontendTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val frontendJsMain by getting {
            dependsOn(frontendMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("io.ktor:ktor-client-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
            }
        }

        val frontendAndroidMain by getting {
            dependsOn(frontendMain)
            dependencies {
                implementation("io.ktor:ktor-client-android")
            }
        }
    }
}

android {
    compileSdk = 32
    // sourceSets["main"].manifest.srcFile("src/frontendAndroidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "kresil.experiments"
}