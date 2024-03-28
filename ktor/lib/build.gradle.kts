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
            implementation(project.dependencies.enforcedPlatform(libs.ktor.bom))
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
                implementation(libs.kotlin.stdlib.jdk8)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.server.calllogging)
                implementation(libs.ktor.server.defaultheaders)
                implementation(libs.ktor.server.sessions)
                implementation(libs.ktor.server.statuspages)
            }
        }

        val backendJvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.client.websockets)
            }
        }

        val frontendMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.websockets)
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
                implementation(libs.kotlin.stdlib.js)
                implementation(libs.ktor.client.js)
                implementation(libs.kotlinx.coroutines.core.js)
            }
        }

        val frontendAndroidMain by getting {
            dependsOn(frontendMain)
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "kresil.experiments"
}

tasks.named("frontendJsBrowserProductionWebpack") {
    dependsOn("backendJvmProcessResources")
}
