import org.jetbrains.kotlin.gradle.DeprecatedTargetPresetApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("kotlin-multiplatform")
}

kotlin {
    @OptIn(DeprecatedTargetPresetApi::class, InternalKotlinGradlePluginApi::class)
    targets {
        js("frontend", IR) {
            browser {
                testTask { enabled = false }

                @OptIn(ExperimentalDistributionDsl::class)
                distribution {
                    directory = file("$projectDir/src/backendMain/resources/web")
                }
                binaries.executable()
            }
        }
        jvm("backend")
    }

    sourceSets.forEach {
        it.dependencies {
            implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:2.3.9"))
        }
    }

    sourceSets {
        val backendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-websockets")
                implementation("io.ktor:ktor-server-call-logging")
                implementation("io.ktor:ktor-server-default-headers")
                implementation("io.ktor:ktor-server-sessions")
                implementation("ch.qos.logback:logback-classic:1.4.6")
            }
        }

        val backendTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host")
                implementation("io.ktor:ktor-client-websockets")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val frontendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("io.ktor:ktor-client-websockets")
                implementation("io.ktor:ktor-client-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.4")
            }
        }
    }
}

tasks.register<JavaExec>("run") {
    dependsOn("frontendBrowserDistribution")
    dependsOn("backendMainClasses")
    mainClass.set("backendMain.ChatApplicationKt")
    // classpath(configurations.getByName("backendRuntimeClasspath").plus("./build/libs/ktor-backend-0.0.1.jar"))
    args = emptyList()
}

tasks.named("frontendBrowserProductionWebpack") {
    mustRunAfter(":ktor:backendProcessResources")
}
