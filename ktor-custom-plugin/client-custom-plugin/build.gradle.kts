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
    implementation(libs.logback.classic)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(project(":end-to-end-utilities"))
}

