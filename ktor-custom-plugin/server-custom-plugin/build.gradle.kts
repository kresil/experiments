plugins {
    application
    alias(libs.plugins.kotlinJvm)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.configyaml)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.testhost)
}
