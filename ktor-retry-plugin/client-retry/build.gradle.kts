plugins {
    application
    alias(libs.plugins.kotlinJvm)
}

application {
    mainClass.set("application.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.hostcommon)
    implementation(libs.logback.classic)
    implementation(project(":simulate-slow-server"))
    implementation(project(":end-to-end-utilities"))
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
}
